package com.zen.ui.fragment.measure.table;

import android.content.Context;
import android.view.View;
import android.widget.ListView;

import com.zen.ui.R;
import com.zen.ui.adapter.MeasureDataSimpleAdapter;
import com.zen.api.Constant;
import com.zen.ui.fragment.measure.utils.FormatUtils;
import com.zen.ui.fragment.measure.utils.TimeUtils;

import java.util.Date;

public class MeasureTableController {

    private final Context context;
    private ListView listView;
    private MeasureDataSimpleAdapter adapter;

    public MeasureTableController(Context context) {
        this.context = context;
    }

    public void attachTo(View root) {
        listView = root.findViewById(R.id.list_view);
        adapter = new MeasureDataSimpleAdapter(context);
        adapter.setData(new java.util.LinkedList<>());
        listView.setAdapter(adapter);
    }

    public void addRow(double value, double temp, String unit, String tempUnitStr) {
        if (adapter == null) return;

        MeasureDataSimpleAdapter.Bean first = adapter.getItem(0);
        MeasureDataSimpleAdapter.Bean bean = new MeasureDataSimpleAdapter.Bean();

        bean.sn = first == null ? 1 : first.sn + 1;

        String dateStr = TimeUtils.format(new Date(), Constant.DateFormat);
        String timeStr = TimeUtils.format(new Date(), Constant.Time2Format);

        bean.date = dateStr;
        bean.time = timeStr;
        bean.value = FormatUtils.formatDouble(value, 3) + " " + unit;
        bean.temp = FormatUtils.formatDouble(temp, 1) + " " + tempUnitStr;

        adapter.addData(bean);
        adapter.notifyDataSetChanged();
    }
}
