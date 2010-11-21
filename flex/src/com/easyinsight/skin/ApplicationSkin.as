package com.easyinsight.skin {
import flash.events.Event;
import flash.events.EventDispatcher;

import mx.controls.Alert;

public class ApplicationSkin extends EventDispatcher {

    private var _coreAppBackgroundImage:Object;
    private var _coreAppBackgroundColor:uint = 0x818285;
    private var _coreAppBackgroundSize:String = "auto";
    private var _headerBarBackgroundColor:uint = 0xF0F0F0;
    private var _headerBarLogo:Class;
    private var _headerBarDividerColor:uint = 0xD42525;
    private var _centerCanvasBackgroundColor:uint = 0xFFFFFF;
    private var _centerCanvasBackgroundAlpha:Number = 1;
    [Embed(source="../../../../assets/background2.JPG")]
    private var _reportBackground:Class;
    private var _reportBackgroundSize:String = "100%";

    public function ApplicationSkin() {
        super();
    }

    private static var _instance:ApplicationSkin;

    public static function instance():ApplicationSkin {
        return _instance;
    }

    public static function initialize():void {
        _instance = new ApplicationSkin();
    }

    public function applyUserSettings(appSkin:ApplicationSkinTO):void {
        if (appSkin.coreAppBackgroundImage != null) {
            var loader:ImageLoader = new ImageLoader();
            loader.addEventListener(ImageLoadEvent.IMAGE_LOADED, function(event:ImageLoadEvent):void {
                coreAppBackgroundImage = event.bitmap;
            });
            loader.load(appSkin.coreAppBackgroundImage.id);
        }
        headerBarBackgroundColor = appSkin.headerBarBackgroundColor;
        headerBarDividerColor = appSkin.headerBarDividerColor;
        centerCanvasBackgroundColor = appSkin.centerCanvasBackgroundColor;
        centerCanvasBackgroundAlpha = appSkin.centerCanvasBackgroundAlpha;
        coreAppBackgroundColor = appSkin.coreAppBackgroundColor;
        coreAppBackgroundSize = appSkin.coreAppBackgroundSize;
    }

    [Bindable(event="reportBackgroundChanged")]
    public function get reportBackground():Class {
        return _reportBackground;
    }

    public function set reportBackground(value:Class):void {
        if (_reportBackground == value) return;
        _reportBackground = value;
        dispatchEvent(new Event("reportBackgroundChanged"));
    }

    [Bindable(event="reportBackgroundSizeChanged")]
    public function get reportBackgroundSize():String {
        return _reportBackgroundSize;
    }

    public function set reportBackgroundSize(value:String):void {
        if (_reportBackgroundSize == value) return;
        _reportBackgroundSize = value;
        dispatchEvent(new Event("reportBackgroundSizeChanged"));
    }

    [Bindable(event="coreAppBackgroundSizeChanged")]
    public function get coreAppBackgroundSize():String {
        return _coreAppBackgroundSize;
    }

    public function set coreAppBackgroundSize(value:String):void {
        if (_coreAppBackgroundSize == value) return;
        _coreAppBackgroundSize = value;
        dispatchEvent(new Event("coreAppBackgroundSizeChanged"));
    }

    [Bindable(event="centerCanvasBackgroundColorChanged")]
    public function get centerCanvasBackgroundColor():uint {
        return _centerCanvasBackgroundColor;
    }

    public function set centerCanvasBackgroundColor(value:uint):void {
        if (_centerCanvasBackgroundColor == value) return;
        _centerCanvasBackgroundColor = value;
        dispatchEvent(new Event("centerCanvasBackgroundColorChanged"));
    }

    [Bindable(event="centerCanvasBackgroundAlphaChanged")]
    public function get centerCanvasBackgroundAlpha():Number {
        return _centerCanvasBackgroundAlpha;
    }

    public function set centerCanvasBackgroundAlpha(value:Number):void {
        if (_centerCanvasBackgroundAlpha == value) return;
        _centerCanvasBackgroundAlpha = value;
        dispatchEvent(new Event("centerCanvasBackgroundAlphaChanged"));
    }

    [Bindable(event="headerBarBackgroundColorChanged")]
    public function get headerBarBackgroundColor():uint {
        return _headerBarBackgroundColor;
    }

    public function set headerBarBackgroundColor(value:uint):void {
        if (_headerBarBackgroundColor == value) return;
        _headerBarBackgroundColor = value;
        dispatchEvent(new Event("headerBarBackgroundColorChanged"));
    }

    [Bindable(event="headerBarLogoChanged")]
    public function get headerBarLogo():Class {
        return _headerBarLogo;
    }

    public function set headerBarLogo(value:Class):void {
        if (_headerBarLogo == value) return;
        _headerBarLogo = value;
        dispatchEvent(new Event("headerBarLogoChanged"));
    }

    [Bindable(event="headerBarDividerColorChanged")]
    public function get headerBarDividerColor():uint {
        return _headerBarDividerColor;
    }

    public function set headerBarDividerColor(value:uint):void {
        if (_headerBarDividerColor == value) return;
        _headerBarDividerColor = value;
        dispatchEvent(new Event("headerBarDividerColorChanged"));
    }

    [Bindable(event="coreAppBackgroundImageChanged")]
    public function get coreAppBackgroundImage():Object {
        return _coreAppBackgroundImage;
    }

    public function set coreAppBackgroundImage(value:Object):void {
        if (_coreAppBackgroundImage == value) return;
        _coreAppBackgroundImage = value;
        dispatchEvent(new Event("coreAppBackgroundImageChanged"));
    }

    [Bindable(event="coreAppBackgroundColorChanged")]
    public function get coreAppBackgroundColor():uint {
        return _coreAppBackgroundColor;
    }

    public function set coreAppBackgroundColor(value:uint):void {
        if (_coreAppBackgroundColor == value) return;
        _coreAppBackgroundColor = value;
        dispatchEvent(new Event("coreAppBackgroundColorChanged"));
    }
}
}