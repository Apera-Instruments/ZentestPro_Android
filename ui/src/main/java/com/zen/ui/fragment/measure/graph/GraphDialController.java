package com.zen.ui.fragment.measure.graph;

import android.view.View;
import android.widget.ImageView;

import com.zen.api.Constant;
import com.zen.ui.R;
import com.zen.ui.fragment.MeasureFragment;
import com.zen.ui.view.MyDashBoardView2;

/**
 * Controls the dial (graph 2) rendering & rotation.
 */
public class GraphDialController {

    private final MeasureFragment fragment;
    private MyDashBoardView2 dashBoard;
    private ImageView ivGraph;
    private ImageView ivDial;

    private double fullValue = 1.0;
    private double lowValue = 0.0;
    private int sweepAngle = 160;
    private float zeroAngle = 0f;

    private int graphModeType = Constant.MODE_VELA_PH;
    private String currentUnit = "";

    public GraphDialController(MeasureFragment fragment) {
        this.fragment = fragment;
    }

    public void attachTo(View root) {
        ivGraph = root.findViewById(R.id.iv_graph);
        ivDial = root.findViewById(R.id.iv_dial);
        dashBoard = new MyDashBoardView2(fragment.getContext());
    }

    public void setGraphMode(int graphModeType, String unit, double currentValue) {
        this.graphModeType = graphModeType;
        this.currentUnit = unit;
        configByMode(graphModeType, unit, currentValue);
        refreshBackground();
    }

    public void updateValue(double value) {
        if (dashBoard == null || ivDial == null) return;

        dashBoard.setMode(graphModeType);
//        dashBoard.setValue(value);
        ivGraph.setImageBitmap(dashBoard.getBitmap());

        if (fullValue <= 0) return;

        float rotation;
        if (value < lowValue) {
            rotation = (float) (sweepAngle * lowValue / fullValue - (sweepAngle / 2) + zeroAngle);
        } else if (value > fullValue) {
            rotation = (float) (sweepAngle * fullValue / fullValue - (sweepAngle / 2) + zeroAngle);
        } else {
            rotation = (float) (sweepAngle * value / fullValue - (sweepAngle / 2) + zeroAngle);
        }
        ivDial.setRotation(rotation);
    }

    private void configByMode(int mode, String unit, double value) {
        // Default
        fullValue = 1.0;
        lowValue = 0;
        sweepAngle = 160;
        zeroAngle = 0;

        switch (mode) {
            case Constant.MODE_VELA_PH:
                dashBoard.setMode(MyDashBoardView2.MODE_OVER);
                dashBoard.setImageResource(R.mipmap.graph_ph);
                fullValue = 18;
                lowValue = -2;
                sweepAngle = 180;
                zeroAngle = 20;
                break;

            case Constant.MODE_VELA_COND:
                dashBoard.setMode(MyDashBoardView2.MODE_ATOP);
                if ("mS".equals(unit)) {
                    dashBoard.setImageResource(R.mipmap.graph_cond_3);
                    fullValue = 20;
                } else if ("µS".equals(unit) || "μS".equals(unit)) {
                    if (value < 200.0) {
                        dashBoard.setImageResource(R.mipmap.graph_cond_1);
                        fullValue = 200;
                    } else {
                        dashBoard.setImageResource(R.mipmap.graph_cond_2);
                        fullValue = 2000;
                    }
                } else {
                    dashBoard.setImageResource(R.mipmap.graph_cond_1);
                    fullValue = 200;
                }
                lowValue = 0;
                sweepAngle = 200;
                zeroAngle = 0;
                break;

            case Constant.MODE_VELA_ORP:
                dashBoard.setMode(MyDashBoardView2.MODE_ATOP);
                dashBoard.setImageResource(R.mipmap.graph_orp);
                fullValue = 2000;
                lowValue = -1000;
                sweepAngle = 180 + 24;
                zeroAngle = sweepAngle / 2f;
                break;

            case Constant.MODE_VELA_RES:
                dashBoard.setMode(MyDashBoardView2.MODE_ATOP);
                if ("Ω".equals(unit)) {
                    dashBoard.setImageResource(R.mipmap.graph_ris_2);
                    fullValue = 1000;
                } else if ("KΩ".equals(unit)) {
                    if (value < 100) {
                        dashBoard.setImageResource(R.mipmap.graph_ris_1);
                        fullValue = 100;
                    } else {
                        dashBoard.setImageResource(R.mipmap.graph_ris_2);
                        fullValue = 1000;
                    }
                } else if ("MΩ".equals(unit)) {
                    dashBoard.setImageResource(R.mipmap.graph_ris_3);
                    fullValue = 20;
                } else {
                    dashBoard.setImageResource(R.mipmap.graph_ris_1);
                    fullValue = 100;
                }
                lowValue = 0;
                sweepAngle = 200;
                zeroAngle = 0;
                break;

            case Constant.MODE_VELA_SALT:
                dashBoard.setMode(MyDashBoardView2.MODE_ATOP);
                if (value < 10.0f) {
                    dashBoard.setImageResource(R.mipmap.graph_sal);
                    fullValue = 10;
                } else {
                    dashBoard.setImageResource(R.mipmap.graph_sal_sea);
                    fullValue = 1000;
                }
                lowValue = 0;
                sweepAngle = 200;
                zeroAngle = 0;
                break;

            case Constant.MODE_VELA_TDS:
                dashBoard.setMode(MyDashBoardView2.MODE_ATOP);
                if (value < 10.0f) {
                    dashBoard.setImageResource(R.mipmap.graph_tds_2);
                    fullValue = 10;
                } else {
                    dashBoard.setImageResource(R.mipmap.graph_tds_1);
                    fullValue = 1000;
                }
                lowValue = 0;
                sweepAngle = 200;
                zeroAngle = 0;
                break;
        }
    }

    private void refreshBackground() {
        if (ivGraph != null && dashBoard != null) {
            ivGraph.setImageBitmap(dashBoard.getBitmap());
        }
    }
}
