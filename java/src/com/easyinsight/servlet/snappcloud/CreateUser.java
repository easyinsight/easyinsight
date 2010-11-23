package com.easyinsight.servlet.snappcloud;

import com.easyinsight.users.UserService;
import com.easyinsight.users.UserTransferObject;
import com.easyinsight.users.AccountTransferObject;
import com.easyinsight.users.Account;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: abaldwin
 * Date: Nov 23, 2010
 * Time: 9:01:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class CreateUser extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean success = true;
        try {
            String userName = request.getParameter("username");
            String firstName = request.getParameter("customerfirstname");
            String lastName = request.getParameter("customerlastname");
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            UserService service = new UserService();
            UserTransferObject user = new UserTransferObject();
            user.setUserName(userName);
            user.setFirstName(firstName);
            user.setName(lastName);
            user.setEmail(email);
            user.setAccountAdmin(true);
            user.setOptInEmail(true);
            AccountTransferObject account = new AccountTransferObject();
            account.setAccountType(Account.PROFESSIONAL);
            service.createAccount(user, account, password);
        } catch(Exception e) {
            success = false;
        }
        response.setStatus(success ? 200 : 422);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        throw new ServletException();
    }
}
