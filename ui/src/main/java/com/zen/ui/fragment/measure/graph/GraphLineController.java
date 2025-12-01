package com.zen.ui.fragment.measure.graph;

import android.graphics.Color;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import im.dacer.androidcharts.LineView;
import com.zen.api.Constant;
import com.zen.ui.R;
import com.zen.ui.fragment.MeasureFragment;
import com.zen.ui.fragment.measure.utils.TimeUtils;

public class GraphLineController {

    private static final int DATA_SIZE1 = 16;

    private final MeasureFragment fragment;
    private LineView lineView;

    private final List<Float> orgData1 = new LinkedList<>();
    private final List<Float> orgData2 = new LinkedList<>();
    private final List<Float> data1 = new LinkedList<>();
    private final List<Float> data2 = new LinkedList<>();
    private final List<String> orgBottomText = new LinkedList<>();
    private final List<String> bottomText = new LinkedList<>();

    private int lineZoom = 1;
    private int lineDateSize = DATA_SIZE1;
    private boolean pendingClear = false;

    public GraphLineController(MeasureFragment fragment) {
        this.fragment = fragment;
    }

    public void attachTo(View root) {
        lineView = root.findViewById(R.id.line_view);
        if (lineView == null) return;

        lineView.setGridCount(5);
        lineView.setDrawDotLine(false);
        lineView.setColorArray(new int[]{
                Color.parseColor("#1ab9f0"),
                Color.parseColor("#a9e65d"),
                Color.GRAY,
                Color.CYAN
        });

        ArrayList<String> initBottom = new ArrayList<>();
        initBottom.add(TimeUtils.nowTimeLabel());
        for (int i = 0; i < lineDateSize; i++) {
            initBottom.add("");
        }
        lineView.setBottomTextList(initBottom);

        List<List<Integer>> initData = new ArrayList<>();
        initData.add(new ArrayList<>());
        initData.add(new ArrayList<>());
        lineView.setDataList(initData);

        lineView.setOnTouchListener(new com.zen.ui.view.OnDoubleClickListener(
                () -> {
                    if (lineZoom == 1) {
                        lineZoom = 16;
                    } else {
                        lineZoom = 1;
                    }
                    Log.i("GraphLineController", "zoom = " + lineZoom);
                    lineView.zoom(1.0f / lineZoom);
                    rebuildFromHistory();
                }));
    }

    public void addPoint(double value, double temp) {
        if (lineView == null) return;

        orgData1.add((float) value);
        orgData2.add((float) temp);
        data1.add((float) value);
        data2.add((float) temp);

        String label = TimeUtils.nowTimeLabel();
        orgBottomText.add(label);
        bottomText.add(label);

        if (data1.size() > lineDateSize) {
            data1.remove(0);
            data2.remove(0);
        }
        if (bottomText.size() > lineDateSize) {
            bottomText.remove(0);
        }

        updateView();
    }

    public void clearHistory() {
        orgData1.clear();
        orgData2.clear();
        data1.clear();
        data2.clear();
        orgBottomText.clear();
        bottomText.clear();

        ArrayList<List<Float>> emptyLists = new ArrayList<>();
        emptyLists.add(new LinkedList<>());
        emptyLists.add(new LinkedList<>());
        if (lineView != null) {
            lineView.setFloatDataList(emptyLists);
        }

        pendingClear = true;
//        fragment.getHandler().postDelayed(() -> {
//            if (pendingClear) {
//                pendingClear = false;
//                rebuildFromHistory();
//            }
//        }, 100);
    }

    private void rebuildFromHistory() {
        if (lineView == null) return;

        ArrayList<List<Float>> lists = new ArrayList<>();
        List<Float> display1 = new LinkedList<>();
        List<Float> display2 = new LinkedList<>();
        List<String> displayBottom = new LinkedList<>();

        int len = orgData1.size();
        int dataSize = DATA_SIZE1 * lineZoom;
        if (len == 0) {
            lists.add(display1);
            lists.add(display2);
            lineView.setFloatDataList(lists);
            return;
        }

        float step = (float) len / (float) dataSize;
        if (step <= 0) step = 1;

        for (float i = 0; i < len; i += step) {
            int idx = (int) i;
            if (idx >= len) break;
            if (display1.size() >= dataSize) break;

            displayBottom.add(orgBottomText.get(idx));
            display1.add(orgData1.get(idx));
            display2.add(orgData2.get(idx));
        }

        lineView.setBottomTextList2(displayBottom);
        lists.add(display1);
        lists.add(display2);
        lineView.updateGridNum();
        lineView.setFloatDataList(lists);
    }

    private void updateView() {
        if (lineView == null) return;

        ArrayList<List<Float>> lists = new ArrayList<>();
        if (lineZoom == 1) {
            lineView.setBottomTextList2(bottomText);
            lists.add(new LinkedList<>(data1));
            lists.add(new LinkedList<>(data2));
        } else {
            rebuildFromHistory();
            return;
        }
        lineView.setFloatDataList(lists);
    }
}

