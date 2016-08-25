package com.petmeds1800.ui.orders.support;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.petmeds1800.R;
import com.petmeds1800.model.entities.CommerceItems;
import com.petmeds1800.model.entities.OrderDetailHeader;
import com.petmeds1800.model.entities.OrderList;
import com.petmeds1800.model.entities.PaymentGroup;
import com.petmeds1800.model.entities.ShippingGroup;
import com.petmeds1800.model.entities.WebViewHeader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pooja on 8/17/2016.
 */
public abstract class CustomOrderDetailRecyclerAdapter extends RecyclerView.Adapter{
    private List<Object> mData;
    private boolean mNotifyOnChange;
    private Context context;

    public CustomOrderDetailRecyclerAdapter(Context context) {
        mData = new ArrayList<Object>();
        mNotifyOnChange=true;
        this.context=context;
    }


    public void setData(OrderList orderList){
        //Add orderdetail header
        mData.add(new OrderDetailHeader(context.getString(R.string.order_detail_header)));
        //Add allCommerce Item in list
        int commerceItemSize= orderList.getCommerceItems().size();
        mData.add(orderList);
        //Add webview Items
        mData.add(new WebViewHeader(context.getString(R.string.cancel_order_header)));
        mData.add(new WebViewHeader(context.getString(R.string.reorder_entire)));


        //Add item header
        mData.add(new OrderDetailHeader(context.getString(R.string.item_header)));

        for(int commerceItemCount=0; commerceItemCount< commerceItemSize;commerceItemCount++){
            CommerceItems commerceItem=orderList.getCommerceItems().get(commerceItemCount);
            mData.add(commerceItem);
        }
        mData.add(new WebViewHeader(context.getString(R.string.reorder_item)));
        mData.add(new WebViewHeader(context.getString(R.string.write_review_header)));


        //Add shipment header
        mData.add(new OrderDetailHeader(context.getString(R.string.shipment_header)));

        //Add all ShippingDetail in List
       int shippingDetailSize=orderList.getShippingGroups().size();
        for(int shippingItemCount=0;shippingItemCount<shippingDetailSize;shippingItemCount++){
            ShippingGroup shippingGroup=orderList.getShippingGroups().get(shippingItemCount);
            mData.add(shippingGroup);
        }
        mData.add(new WebViewHeader(context.getString(R.string.track_shipment)));

        //Add payment header
        mData.add(new OrderDetailHeader(context.getString(R.string.payment_header)));

        //Add all PaymentDetail in List
        int paymentDetailSize=orderList.getPaymentGroups().size();
        for(int paymentItemCount=0;paymentItemCount<paymentDetailSize;paymentItemCount++){
            PaymentGroup paymentGroup=orderList.getPaymentGroups().get(paymentItemCount);
            mData.add(paymentGroup);
        }

        if (mNotifyOnChange)
            notifyItemRangeInserted(0, mData.size());


    }



    @Override
    public int getItemCount() {
        return mData.size();
    }

    public Object getItemAt(int position) {
        Log.d("mdata size", mData.size() + ">>>");
        return mData.get(position);
    }
    public void add(Object item) {
        if (item != null) {
            mData.add(item);
            if (mNotifyOnChange)
                notifyItemChanged(mData.size() - 1);
        }
    }
    public void refreshData(OrderList data) {
        clear();
        setData(data);
    }
    /**
     * Clear all items and groups.
     */
    public void clear() {
        if (mData.size() > 0) {
            int size = mData.size();
            mData.clear();
            if (mNotifyOnChange)
                notifyItemRangeRemoved(0, size);
        }
    }
    public void insert(int position, Object item) {
        mData.add(position, item);
        if (mNotifyOnChange)
            notifyItemInserted(position);
    }
}