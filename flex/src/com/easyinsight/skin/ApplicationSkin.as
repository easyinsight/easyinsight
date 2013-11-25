package com.easyinsight.skin {
import com.easyinsight.framework.User;

import flash.events.Event;
import flash.events.EventDispatcher;

import mx.collections.ArrayCollection;

public class ApplicationSkin extends EventDispatcher {

    private var _coreAppBackgroundImage:Object;
    private var _coreAppBackgroundColor:uint = 0x818285;
    private var _coreAppBackgroundSize:String = "auto";
    private var _headerBarBackgroundColor:uint = 0xF0F0F0;
    private var _headerBarLogo:Object;
    private var _reportHeaderImage:Object;
    private var _headerBarDividerColor:uint = 0xD42525;
    private var _centerCanvasBackgroundColor:uint = 0xFFFFFF;
    private var _centerCanvasBackgroundAlpha:Number = 1;
    private var _headerBackgroundColor:uint;
    private var _headerTextColor:uint;
    private var _reportHeader:Boolean;

    private var _myDataName:Boolean = true;
    private var _myDataSize:Boolean = false;
    private var _myDataOwner:Boolean = false;
    private var _myDataCreationDate:Boolean = false;
    private var _myDataLastTime:Boolean = false;
    private var _myDataCombine:Boolean = false;
    private var _myDataNewScorecard:Boolean = true;
    private var _myDataNewKPITree:Boolean = false;
    private var _myDataNewDashboard:Boolean = true;
    private var _myDataLookupTable:Boolean = false;
    private var _myDataAccountVisible:Boolean = false;

    private var _dataSourceTags:Boolean = false;
    private var _dataSourceAPIKey:Boolean = false;
    private var _reportTags:Boolean = false;
    private var _reportType:Boolean = false;
    private var _reportAPIKey:Boolean = false;
    private var _reportModified:Boolean = false;
    private var _reportCached:Boolean = false;
    private var _reportPersistedCached:Boolean = false;

    public var customChartColor:int;
    public var customChartColorEnabled:Boolean;
    public var gradientChartColor:int;
    public var gradientChartColorEnabled:Boolean;

    public var multiColors:ArrayCollection;

    public var dashboardStack1ColorStart:int;
    public var dashboardStack1ColorEnd:int;
    public var dashboardStackColor2Start:int;
    public var dashboardStackColor2End:int;

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
        headerBackgroundColor = appSkin.reportBackgroundColor;
        headerTextColor = appSkin.reportTextColor;
        reportHeader = appSkin.reportHeader;

        customChartColor = appSkin.customChartColor;
        customChartColorEnabled = appSkin.customChartColorEnabled;
        gradientChartColor = appSkin.gradientChartColor;
        gradientChartColorEnabled = appSkin.gradientChartColorEnabled;
        dashboardStack1ColorStart = appSkin.dashboardStack1ColorStart;
        dashboardStack1ColorEnd = appSkin.dashboardStack1ColorEnd;
        dashboardStackColor2Start = appSkin.dashboardStackColor2Start;
        dashboardStackColor2End = appSkin.dashboardStackColor2End;
        multiColors = appSkin.multiColors;

        if (appSkin.reportHeaderImage != null) {
            var reportHeaderImageLoader:ImageLoader = new ImageLoader();
            reportHeaderImageLoader.addEventListener(ImageLoadEvent.IMAGE_LOADED, function(event:ImageLoadEvent):void {
                User.getInstance().reportLogo = event.bitmap;
                reportHeaderImage = event.bitmap;
            });
            reportHeaderImageLoader.load(appSkin.reportHeaderImage.id);
        } else {
            User.getInstance().reportLogo = null;
        }

        if (appSkin.headerBarLogo != null) {
            var headerBarLoader:ImageLoader = new ImageLoader();
            headerBarLoader.addEventListener(ImageLoadEvent.IMAGE_LOADED, function(event:ImageLoadEvent):void {
                headerBarLogo = event.bitmap;
            });
            headerBarLoader.load(appSkin.headerBarLogo.id);
        }
        myDataName = appSkin.myDataName;
        myDataSize = appSkin.myDataSize;
        myDataOwner = appSkin.myDataOwner;
        myDataCreationDate = appSkin.myDataCreationDate;
        myDataLastTime = appSkin.myDataLastTime;
        myDataCombine = appSkin.myDataCombine;
        myDataNewScorecard = appSkin.myDataNewScorecard;
        myDataNewDashboard = appSkin.myDataNewDashboard;
        myDataLookupTable = appSkin.myDataLookupTable;
        myDataAccountVisible = appSkin.myDataAccountVisible;

        dataSourceTags = appSkin.dataSourceTags;
        dataSourceAPIKey = appSkin.dataSourceAPIKey;
        reportTags = appSkin.reportTags;
        reportType = appSkin.reportType;
        reportAPIKey = appSkin.reportAPIKey;
        reportModified = appSkin.reportModified;
        reportCached = appSkin.reportCached;
        reportPersistedCached = appSkin.reportPersistedCached;
    }

    [Bindable(event="dataSourceTagsChanged")]
    public function get dataSourceTags():Boolean {
        return _dataSourceTags;
    }

    public function set dataSourceTags(value:Boolean):void {
        if (_dataSourceTags == value) return;
        _dataSourceTags = value;
        dispatchEvent(new Event("dataSourceTagsChanged"));
    }

    [Bindable(event="dataSourceAPIKeyChanged")]
    public function get dataSourceAPIKey():Boolean {
        return _dataSourceAPIKey;
    }

    public function set dataSourceAPIKey(value:Boolean):void {
        if (_dataSourceAPIKey == value) return;
        _dataSourceAPIKey = value;
        dispatchEvent(new Event("dataSourceAPIKeyChanged"));
    }

    [Bindable(event="reportTagsChanged")]
    public function get reportTags():Boolean {
        return _reportTags;
    }

    public function set reportTags(value:Boolean):void {
        if (_reportTags == value) return;
        _reportTags = value;
        dispatchEvent(new Event("reportTagsChanged"));
    }

    [Bindable(event="reportTypeChanged")]
    public function get reportType():Boolean {
        return _reportType;
    }

    public function set reportType(value:Boolean):void {
        if (_reportType == value) return;
        _reportType = value;
        dispatchEvent(new Event("reportTypeChanged"));
    }

    [Bindable(event="reportAPIKeyChanged")]
    public function get reportAPIKey():Boolean {
        return _reportAPIKey;
    }

    public function set reportAPIKey(value:Boolean):void {
        if (_reportAPIKey == value) return;
        _reportAPIKey = value;
        dispatchEvent(new Event("reportAPIKeyChanged"));
    }

    [Bindable(event="reportModifiedChanged")]
    public function get reportModified():Boolean {
        return _reportModified;
    }

    public function set reportModified(value:Boolean):void {
        if (_reportModified == value) return;
        _reportModified = value;
        dispatchEvent(new Event("reportModifiedChanged"));
    }

    [Bindable(event="reportCachedChanged")]
    public function get reportCached():Boolean {
        return _reportCached;
    }

    public function set reportCached(value:Boolean):void {
        if (_reportCached == value) return;
        _reportCached = value;
        dispatchEvent(new Event("reportCachedChanged"));
    }

    [Bindable(event="reportPersistedCachedChanged")]
    public function get reportPersistedCached():Boolean {
        return _reportPersistedCached;
    }

    public function set reportPersistedCached(value:Boolean):void {
        if (_reportPersistedCached == value) return;
        _reportPersistedCached = value;
        dispatchEvent(new Event("reportPersistedCachedChanged"));
    }

    [Bindable(event="reportHeaderImageChanged")]
    public function get reportHeaderImage():Object {
        return _reportHeaderImage;
    }

    public function set reportHeaderImage(value:Object):void {
        if (_reportHeaderImage == value) return;
        _reportHeaderImage = value;
        dispatchEvent(new Event("reportHeaderImageChanged"));
    }

    [Bindable(event="reportHeaderChanged")]
    public function get reportHeader():Boolean {
        return _reportHeader;
    }

    public function set reportHeader(value:Boolean):void {
        if (_reportHeader == value) return;
        _reportHeader = value;
        dispatchEvent(new Event("reportHeaderChanged"));
    }

    [Bindable(event="headerBackgroundColorChanged")]
    public function get headerBackgroundColor():uint {
        return _headerBackgroundColor;
    }

    public function set headerBackgroundColor(value:uint):void {
        if (_headerBackgroundColor == value) return;
        _headerBackgroundColor = value;
        dispatchEvent(new Event("headerBackgroundColorChanged"));
    }

    [Bindable(event="headerTextColorChanged")]
    public function get headerTextColor():uint {
        return _headerTextColor;
    }

    public function set headerTextColor(value:uint):void {
        if (_headerTextColor == value) return;
        _headerTextColor = value;
        dispatchEvent(new Event("headerTextColorChanged"));
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
    public function get headerBarLogo():Object {
        return _headerBarLogo;
    }

    public function set headerBarLogo(value:Object):void {
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


    [Bindable(event="myDataNameChanged")]
    public function get myDataName():Boolean {
        return _myDataName;
    }

    public function set myDataName(value:Boolean):void {
        if (_myDataName == value) return;
        _myDataName = value;
        dispatchEvent(new Event("myDataNameChanged"));
    }

    [Bindable(event="myDataSizeChanged")]
    public function get myDataSize():Boolean {
        return _myDataSize;
    }

    public function set myDataSize(value:Boolean):void {
        if (_myDataSize == value) return;
        _myDataSize = value;
        dispatchEvent(new Event("myDataSizeChanged"));
    }

    [Bindable(event="myDataOwnerChanged")]
    public function get myDataOwner():Boolean {
        return _myDataOwner;
    }

    public function set myDataOwner(value:Boolean):void {
        if (_myDataOwner == value) return;
        _myDataOwner = value;
        dispatchEvent(new Event("myDataOwnerChanged"));
    }

    [Bindable(event="myDataCreationDateChanged")]
    public function get myDataCreationDate():Boolean {
        return _myDataCreationDate;
    }

    public function set myDataCreationDate(value:Boolean):void {
        if (_myDataCreationDate == value) return;
        _myDataCreationDate = value;
        dispatchEvent(new Event("myDataCreationDateChanged"));
    }

    [Bindable(event="myDataLastTimeChanged")]
    public function get myDataLastTime():Boolean {
        return _myDataLastTime;
    }

    public function set myDataLastTime(value:Boolean):void {
        if (_myDataLastTime == value) return;
        _myDataLastTime = value;
        dispatchEvent(new Event("myDataLastTimeChanged"));
    }

    [Bindable(event="myDataCombineChanged")]
    public function get myDataCombine():Boolean {
        return _myDataCombine;
    }

    public function set myDataCombine(value:Boolean):void {
        if (_myDataCombine == value) return;
        _myDataCombine = value;
        dispatchEvent(new Event("myDataCombineChanged"));
    }

    [Bindable(event="myDataNewScorecardChanged")]
    public function get myDataNewScorecard():Boolean {
        return _myDataNewScorecard;
    }

    public function set myDataNewScorecard(value:Boolean):void {
        if (_myDataNewScorecard == value) return;
        _myDataNewScorecard = value;
        dispatchEvent(new Event("myDataNewScorecardChanged"));
    }

    [Bindable(event="myDataNewKPITreeChanged")]
    public function get myDataNewKPITree():Boolean {
        return _myDataNewKPITree;
    }

    public function set myDataNewKPITree(value:Boolean):void {
        if (_myDataNewKPITree == value) return;
        _myDataNewKPITree = value;
        dispatchEvent(new Event("myDataNewKPITreeChanged"));
    }

    [Bindable(event="myDataNewDashboardChanged")]
    public function get myDataNewDashboard():Boolean {
        return _myDataNewDashboard;
    }

    public function set myDataNewDashboard(value:Boolean):void {
        if (_myDataNewDashboard == value) return;
        _myDataNewDashboard = value;
        dispatchEvent(new Event("myDataNewDashboardChanged"));
    }

    [Bindable(event="myDataLookupTableChanged")]
    public function get myDataLookupTable():Boolean {
        return _myDataLookupTable;
    }

    public function set myDataLookupTable(value:Boolean):void {
        if (_myDataLookupTable == value) return;
        _myDataLookupTable = value;
        dispatchEvent(new Event("myDataLookupTableChanged"));
    }


    [Bindable(event="myDataAccountVisibleChanged")]
    public function get myDataAccountVisible():Boolean {
        return _myDataAccountVisible;
    }

    public function set myDataAccountVisible(value:Boolean):void {
        if (_myDataAccountVisible == value) return;
        _myDataAccountVisible = value;
        dispatchEvent(new Event("myDataAccountVisibleChanged"));
    }
}
}