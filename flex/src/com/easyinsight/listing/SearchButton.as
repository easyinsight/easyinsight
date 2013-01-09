/**
 * Created by ${PRODUCT_NAME}.
 * User: jamesboe
 * Date: 4/28/11
 * Time: 2:52 PM
 * To change this template use File | Settings | File Templates.
 */
package com.easyinsight.listing {
import com.easyinsight.dashboard.DashboardContextWindow;
import com.easyinsight.dashboard.DashboardDescriptor;
import com.easyinsight.framework.PerspectiveInfo;
import com.easyinsight.framework.User;
import com.easyinsight.genredata.AnalyzeEvent;
import com.easyinsight.quicksearch.EIDescriptor;
import com.easyinsight.quicksearch.QuickSearchEvent;
import com.easyinsight.quicksearch.QuickSearchWindow;

import com.easyinsight.report.ReportPerspectiveInfo;
import com.easyinsight.solutions.DataSourceDescriptor;
import com.easyinsight.solutions.InsightDescriptor;
import com.easyinsight.util.PopUpUtil;

import flash.events.Event;

import flash.events.MouseEvent;

import mx.controls.Button;
import mx.managers.PopUpManager;

public class SearchButton extends Button {

    public function SearchButton() {
        styleName = "altGrayButton";
        label = "Search";
        addEventListener(MouseEvent.CLICK, onClick);
    }

    private function onClick(event:MouseEvent):void {
        var window:QuickSearchWindow = new QuickSearchWindow();
        window.addEventListener(QuickSearchEvent.QUICK_SEARCH, onQuickSearch, false, 0, true);
        window.addEventListener(QuickSearchEvent.QUICK_SEARCH_CANCEL, onQuickSearch, false, 0, true);
        PopUpManager.addPopUp(window, this, false);
        PopUpUtil.centerPopUp(window);
    }

    private function onQuickSearch(event:QuickSearchEvent):void {
        event.currentTarget.removeEventListener(QuickSearchEvent.QUICK_SEARCH, onQuickSearch);
        event.currentTarget.removeEventListener(QuickSearchEvent.QUICK_SEARCH_CANCEL, onQuickSearch);
        if (event.type == QuickSearchEvent.QUICK_SEARCH) {
            if (event.eiDescriptor.getType() == EIDescriptor.DATA_SOURCE) {
                var eiDescriptor:DataSourceDescriptor = event.eiDescriptor as DataSourceDescriptor;
                User.getEventNotifier().dispatchEvent(new AnalyzeEvent(new PerspectiveInfo(PerspectiveInfo.REPORT_EDITOR, {dataSourceID: eiDescriptor.id})));
            } else if (event.eiDescriptor.getType() == EIDescriptor.REPORT) {
                User.getEventNotifier().dispatchEvent(new AnalyzeEvent(new ReportPerspectiveInfo(event.eiDescriptor as InsightDescriptor)));
            } else if (event.eiDescriptor.getType() == EIDescriptor.DASHBOARD) {
                if (User.getInstance().analyst) {
                    var dashboardWindow:DashboardContextWindow = new DashboardContextWindow();
                    dashboardWindow.dashboardDescriptor = event.eiDescriptor as DashboardDescriptor;
                    dashboardWindow.addEventListener(AnalyzeEvent.ANALYZE, onAnalyze, false, 0, true);
                    PopUpManager.addPopUp(dashboardWindow, this, false);
                    PopUpUtil.centerPopUp(dashboardWindow);
                } else {
                    User.getEventNotifier().dispatchEvent(new AnalyzeEvent(new PerspectiveInfo(PerspectiveInfo.DASHBOARD_VIEW, {dashboardID: event.eiDescriptor.id})));
                }
            } else if (event.eiDescriptor.getType() == EIDescriptor.SCORECARD) {
                User.getEventNotifier().dispatchEvent(new AnalyzeEvent(new PerspectiveInfo(PerspectiveInfo.SCORECARD_VIEW, {scorecardID: event.eiDescriptor.id})));
            }
        }
    }

    private function onAnalyze(event:Event):void {
        User.getEventNotifier().dispatchEvent(event);
    }
}
}
