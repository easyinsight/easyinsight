package com.easyinsight.genredata {
import flash.events.Event;

import flash.events.EventDispatcher;

import mx.binding.utils.BindingUtils;
import mx.collections.ArrayCollection;
import mx.collections.Sort;
import mx.collections.SortField;
import mx.core.IFactory;

[Event(name="changeView", type="com.easyinsight.genredata.ExchangeControllerEvent")]
[Event(name="updateURL", type="flash.events.Event")]
public class ExchangeController extends EventDispatcher {

    private var _exchangeGridPage:ExchangePage;
    private var _dataProvider:ArrayCollection = new ArrayCollection();
    private var _selectedTag:String;
    private var _keyword:String;
    private var _selectedPage:ExchangePage;
    private var _subTopicID:int;
    private var _loading:Boolean;


    [Bindable(event="loadingChanged")]
    public function get loading():Boolean {
        return _loading;
    }

    public function set loading(value:Boolean):void {
        if (_loading == value) return;
        _loading = value;
        dispatchEvent(new Event("loadingChanged"));
    }

    public function ExchangeController() {
        addEventListener(ExchangeDataEvent.EXCHANGE_DATA_RETURNED, onDataReturned);
    }

    public function initBehavior():void {        
    }

    public function get text():String {
        return "";
    }

    public function get subTopicID():int {
        return _subTopicID;
    }

    public function set subTopicID(value:int):void {
        _subTopicID = value;
    }

    public function get selectedPage():ExchangePage {
        if (_selectedPage == null) {
            _selectedPage = exchangeGridPage;
        }
        return _selectedPage;
    }

    [Bindable(event="selectedTagChanged")]
    public function get selectedTag():String {
        return _selectedTag;
    }

    public function set selectedTag(value:String):void {
        if (_selectedTag == value) return;
        _selectedTag = value;
        if (dataProvider2 != null) {
            dataProvider2.refresh();
        }
        dispatchEvent(new Event("selectedTagChanged"));
    }

    [Bindable(event="keywordChanged")]
    public function get keyword():String {
        return _keyword;
    }

    public function set keyword(value:String):void {
        if (value != null) {
            value = value.toLowerCase();
        }
        if (_keyword == value) return;
        _keyword = value;
        if (dataProvider2 != null) {
            dataProvider2.refresh();
        }
        dispatchEvent(new Event("keywordChanged"));
    }

    [Bindable(event="dataProviderChanged")]
    public function get dataProvider2():ArrayCollection {
        return _dataProvider;
    }

    public function set dataProvider2(value:ArrayCollection):void {
        if (_dataProvider == value) return;
        _dataProvider = value;
        if (_dataProvider != null) {
            _dataProvider.filterFunction = filterData;
            _dataProvider.refresh();
        }
        dispatchEvent(new Event("dataProviderChanged"));
    }

    private function configureExchangePage(exchangePage:ExchangePage):void {
        BindingUtils.bindProperty(exchangePage, "loading", this, "loading");
        BindingUtils.bindProperty(exchangePage, "dataProvider2", this, "dataProvider2");
        BindingUtils.bindProperty(exchangePage, "selectedTag", this, "selectedTag");
        BindingUtils.bindProperty(this, "selectedTag", exchangePage, "selectedTag");
        BindingUtils.bindProperty(exchangePage, "keyword", this, "keyword");
    }

    protected function filterData(object:Object):Boolean {
        return false;
    }

    private function onDataReturned(event:ExchangeDataEvent):void {
        loading = false;
        var data:ArrayCollection = event.data;
        data.filterFunction = filterData;
        if (dataProvider2.sort == null) {
            var sort:Sort = new Sort();
            var sortField:SortField = new SortField();
            sortField.name = "ratingAverage";
            sortField.descending = true;
            sort.fields = [ sortField ];
            data.sort = sort;
        } else {
            data.sort = dataProvider2.sort;
        }
        data.refresh();
        dataProvider2 = data;
    }

    public function get exchangeGridPage():ExchangePage {
        if (_exchangeGridPage == null) {
            _exchangeGridPage = createExchangedGridPage();
            configureExchangePage(_exchangeGridPage);
        }
        return _exchangeGridPage;
    }

    protected function createExchangedGridPage():ExchangePage {
        return null;
    }

    public function refreshData():void {
        loading = true;
        retrieveData();
    }

    protected function summaryItemRenderer():IFactory {
        return null;
    }

    protected function retrieveData():void {
    }

    public function decorateObject(fragmentObject:Object):void {
    }
}
}