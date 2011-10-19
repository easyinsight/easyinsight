package com.easyinsight.dashboard {
import com.easyinsight.skin.ImageBox;
import com.easyinsight.skin.ImageUploadEvent;

import flash.events.MouseEvent;

import mx.collections.ArrayCollection;

import mx.containers.VBox;
import mx.controls.Label;

public class DashboardImageEditorComponent extends VBox implements IDashboardEditorComponent {

    public var image:DashboardImage;

    private var imageBox:ImageBox;

    public function DashboardImageEditorComponent() {
        super();
        setStyle("verticalAlign", "middle");
        setStyle("horizontalAlign", "center");
        this.percentWidth = 100;
        this.percentHeight = 100;
    }

    public function obtainPreferredSizeInfo():SizeInfo {
        return new SizeInfo();
    }

    protected override function createChildren():void {
        super.createChildren();
        imageBox = new ImageBox();
        imageBox.imageDescriptor = image.imageDescriptor;
        imageBox.addEventListener(ImageUploadEvent.IMAGE_UPLOAD, onImageUpload);
        addChild(imageBox);
    }

    private function onImageUpload(event:ImageUploadEvent):void {
        setStyle("borderColor", "red");
        setStyle("borderThickness", 0);
        setStyle("borderStyle", "none");
        errorString = null;
    }

    public function save():void {
        image.imageDescriptor = imageBox.imageDescriptor;
    }

    public function validate():Boolean {
        if (image.imageDescriptor == null) {
            setStyle("borderColor", "red");
            setStyle("borderThickness", 1);
            setStyle("borderStyle", "solid");
            errorString = "You need to configure this section of the grid.";
            dispatchEvent(new MouseEvent(MouseEvent.MOUSE_OVER));
        }
        return image.imageDescriptor != null;
    }

    public function edit():void {
    }

    public function refresh():void {
    }

    public function updateAdditionalFilters(filterMap:Object):void {
    }

    public function initialRetrieve():void {
    }

    public function reportCount():ArrayCollection {
        return null;
    }

    public function toggleFilters(showFilters:Boolean):void {
    }
}
}