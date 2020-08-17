package com.part5.view.recycler.refresh.sample;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import com.part5.util.ResUtil;
import com.part5.view.recycler.refresh.AbsHolder;

public class MyHolder extends AbsHolder<String> {
    public MyHolder(Context context, final MyAdapter adapter) {
        super(new TextView(context), adapter);
        itemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ResUtil.dpToPx(70)));
        itemView.setBackgroundColor(0xFFFFFFFF);
        itemView.setPadding(ResUtil.dpToPx(10), 0, 0, 0);
        itemView.setOnClickListener(v -> adapter.getOnSRVAdapterListener().onItemClick(v, position));
        ((TextView) itemView).setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        ((TextView) itemView).setTextColor(0xFF666666);
        ((TextView) itemView).setGravity(Gravity.CENTER_VERTICAL);
        Drawable drawable = context.getResources().getDrawable(android.R.mipmap.sym_def_app_icon);
        drawable.setBounds(0, 0, ResUtil.dpToPx(33), ResUtil.dpToPx(33));
        ((TextView) itemView).setCompoundDrawables(drawable, null, null, null);
        ((TextView) itemView).setCompoundDrawablePadding(ResUtil.dpToPx(10));
    }

    @Override
    public void onBindViewHolder(int position) {
        super.onBindViewHolder(position);
        String item = adapter.getItem(position);
        ((TextView) itemView).setText(item);
    }
}
