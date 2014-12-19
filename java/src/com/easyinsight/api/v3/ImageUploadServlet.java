package com.easyinsight.api.v3;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.easyinsight.database.Database;
import com.easyinsight.database.EIConnection;
import com.easyinsight.export.ExportMetadata;
import com.easyinsight.export.ExportService;
import com.easyinsight.logging.LogClass;
import com.easyinsight.preferences.ImageDescriptor;
import com.easyinsight.preferences.PreferencesService;
import com.easyinsight.users.UserService;
import net.minidev.json.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONArray;
import org.json.JSONException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * User: jamesboe
 * Date: 7/8/13
 * Time: 1:26 PM
 */
@WebServlet(value = "/images", asyncSupported = true)
public class ImageUploadServlet extends JSONServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        authProcessor(req, resp, () ->
        Database.useConnection(true, (conn) -> {
            ExportMetadata md = ExportService.createExportMetadata(conn);
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            String contentType = null;
            List items = upload.parseRequest(req);
            byte[] bytes = null;
            String fileName = null;
            for (Object obj : items) {
                FileItem fileItem = (FileItem) obj;
                System.out.println("File item " + fileItem.getFieldName() + " - " + fileItem.getName() + " - " + fileItem.getContentType() + " - " + fileItem.getSize());
                fileName = fileItem.getName();
                contentType = fileItem.getContentType();
                if (fileItem.isFormField()) {
                } else if (fileItem.getSize() > 0) {
                    bytes = fileItem.get();
                    System.out.println("got " + bytes.length + " bytes");
                }
            }
            org.json.JSONObject jo = new org.json.JSONObject();
            ImageDescriptor id = null;
            if(bytes != null && bytes.length > 0 && bytes.length < 1024 * 1024 * 10 && fileName != null && fileName.length() > 0 && isImage(contentType)) {
                id = new PreferencesService().createImage(conn, fileName, contentType, bytes, false);
                jo.put("image", id.toJSON(md));
            }
            ResponseInfo ri = new ResponseInfo(ResponseInfo.ALL_GOOD, jo.toString());
            resp.setContentType("application/json");
            resp.setStatus(ri.getCode());
            resp.getOutputStream().write(ri.getResponseBody().getBytes());
            resp.getOutputStream().flush();
        }));
    }

    private boolean isImage(String contentType) {
        /* image/gif: GIF image; Defined in RFC 2045 and RFC 2046
        image/jpeg: JPEG JFIF image; Defined in RFC 2045 and RFC 2046
        image/pjpeg: JPEG JFIF image; Associated with Internet Explorer; Listed in ms775147(v=vs.85) - Progressive JPEG, initiated before global browser support for progressive JPEGs (Microsoft and Firefox).
        image/png: Portable Network Graphics; Registered,[12] Defined in RFC 2083
        image/svg+xml: SVG vector image; Defined in SVG Tiny 1.2 Specification Appendix M
        image/vnd.djvu: DjVu image and multipage document format.[13]
        image/example: example in documentation, Defined in RFC 4735 */
        return contentType != null && contentType.matches("^image/(gif|jpeg|pjpeg|png|svg\\+xml)$");
    }

    @Override
    protected ResponseInfo processGet(JSONObject jsonObject, EIConnection conn, HttpServletRequest request) throws Exception {
        List<ImageDescriptor> images = new PreferencesService().getImages();
        ExportMetadata md = ExportService.createExportMetadata(conn);
        org.json.JSONObject jo = new org.json.JSONObject();
        JSONArray ja = new org.json.JSONArray(images.stream().map(a -> {
            try {
                return a.toJSON(md);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList()));
        jo.put("images", ja);
        return new ResponseInfo(ResponseInfo.ALL_GOOD, jo.toString());
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        authProcessor(req, resp, () ->
            Database.useConnection(true, (conn) -> {

            })
        );
    }

    @Override
    protected ResponseInfo processJSON(JSONObject jsonObject, EIConnection conn, HttpServletRequest request) throws Exception {
        throw new UnsupportedOperationException();
    }
}
