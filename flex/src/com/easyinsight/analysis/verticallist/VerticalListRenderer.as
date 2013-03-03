<?xml version="1.0"?>
    <mx:VBox xmlns:mx="http://www.adobe.com/2006/mxml" xmlns:util="com.easyinsight.util.*"
    implements="com.easyinsight.analysis.IReportExtensionEditor" label="YTD"
    paddingLeft="20" paddingBottom="20" paddingRight="20" paddingTop="20">
        <mx:Script><![CDATA[
        import com.easyinsight.analysis.ytd.YTDReportFieldExtension;

        import mx.collections.ArrayCollection;

        [Bindable]
        private var dateFields:ArrayCollection;

        private var _analysisItem:AnalysisItem;

        public function save(analysisItem:AnalysisItem):void {
            var ext:YTDReportFieldExtension = new YTDReportFieldExtension();
            ext.benchmark = dateComboBox.selectedItem as AnalysisItem;
            ext.lineAbove = verticalLineBox.selected;
            ext.alwaysShow = alwaysShowBox.selected;
            analysisItem.reportFieldExtension = ext;
        }

        public function set analysisItem(analysisItem:AnalysisItem):void {
            _analysisItem = analysisItem;
            var ext:YTDReportFieldExtension = _analysisItem.reportFieldExtension as YTDReportFieldExtension;
            if (ext != null) {
                if (ext.benchmark != null) {
                    selectedValue = ext.benchmark.display;
                }
                alwaysShow = ext.alwaysShow;
                verticalLineAbove = ext.lineAbove;
            }
        }

        public function set analysisItems(analysisItems:ArrayCollection):void {
            var dateFields:ArrayCollection = new ArrayCollection();
            for each (var analysisItemWrapper:AnalysisItemWrapper in analysisItems) {
                if (analysisItemWrapper.analysisItem.hasType(AnalysisItemTypes.MEASURE)) {
                    dateFields.addItem(analysisItemWrapper.analysisItem);
                }
            }
            rowCount = Math.min(10, dateFields.length);
            dateFields.addItemAt({ display: '[ No Benchmark ]'}, 0);
            this.dateFields = dateFields;
        }

        [Bindable]
        private var rowCount:int;


        [Bindable]
        private var selectedValue:String;
        
        [Bindable]
        private var verticalLineAbove:Boolean;

        [Bindable]
        private var alwaysShow:Boolean;
        ]]></mx:Script>
        <mx:Form>
            <mx:FormItem label="Which field should serve as a benchmark?">
                <util:SmartComboBox dataProvider="{dateFields}" id="dateComboBox" labelField="display" selectedValue="{selectedValue}" selectedProperty="display"
                rowCount="{rowCount}"/>
            </mx:FormItem>
            <mx:FormItem label="Draw a vertical line over this field:">
                <mx:CheckBox id="verticalLineBox" selected="{verticalLineAbove}"/>
            </mx:FormItem>
            <mx:FormItem label="Always show this field:">
                <mx:CheckBox id="alwaysShowBox" selected="{alwaysShow}"/>
            </mx:FormItem>
        </mx:Form>


    </mx:VBox>
