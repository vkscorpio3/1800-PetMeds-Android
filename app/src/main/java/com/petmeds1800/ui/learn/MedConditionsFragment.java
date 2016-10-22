package com.petmeds1800.ui.learn;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.petmeds1800.R;
import com.petmeds1800.model.entities.NameValueData;
import com.petmeds1800.ui.fragments.AbstractFragment;
import com.petmeds1800.ui.fragments.CommonWebviewFragment;
import com.petmeds1800.ui.fragments.LearnFragment;
import com.petmeds1800.util.Utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Digvijay on 10/18/2016.
 */

public class MedConditionsFragment extends AbstractFragment implements MedConditionsListContract.View, MedConditionsListAdapter.OnItemClickListener {

    @BindView(R.id.error_layout)
    LinearLayout mErrorLayout;

    @BindView(R.id.error_Text)
    TextView mErrorLabel;

    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;

    @BindView(R.id.container_layout)
    RelativeLayout mContainerLayout;

    @BindView(R.id.recycler_med_conditions)
    RecyclerView mRecyclerMedConditions;

    private MedConditionsListAdapter mListAdapter;

    private List<NameValueData> mMedConditionList = new ArrayList<>();

    private MedConditionsListContract.Presenter mPresenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mPresenter = new MedConditionsListPresenter(this, getContext());
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conditions, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerMedConditions.setLayoutManager(layoutManager);
        mListAdapter = new MedConditionsListAdapter(mMedConditionList);
        mListAdapter.setOnItemClickListener(this);
        mRecyclerMedConditions.setAdapter(mListAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.getConditionsList();
    }

    @Override
    public void showProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void populateConditionsListView(List<NameValueData> medConditionList) {
        if (medConditionList != null) {
            mMedConditionList.clear();
            mMedConditionList.addAll(medConditionList);
            mListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void showErrorCrouton(CharSequence message, boolean span) {
        message = message.equals(Utils.TIME_OUT) ? getString(R.string.internet_not_available) : message;
        mProgressBar.setVisibility(View.GONE);
        if (span) {
            Utils.displayCrouton(getActivity(), (Spanned) message, mContainerLayout);
        } else {
            Utils.displayCrouton(getActivity(), (String) message, mContainerLayout);
        }
    }

    @Override
    public void showWebViewFragment(String medConditionName) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString(CommonWebviewFragment.TITLE_KEY, medConditionName);
            String encodedQuery = URLEncoder.encode(medConditionName, "utf-8");
            bundle.putString(CommonWebviewFragment.URL_KEY, getString(R.string.url_med_condition) + encodedQuery);
            ((LearnFragment) getParentFragment()).addAskVetFragment(bundle);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showRetryView(String errorMessage) {
        mErrorLayout.setVisibility(View.VISIBLE);
        mErrorLabel.setText(errorMessage);
    }

    @Override
    public void setPresenter(MedConditionsListContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @OnClick(R.id.retry_Button)
    public void retry() {
        mErrorLayout.setVisibility(View.GONE);
        showProgress();
        mPresenter.getConditionsList();
    }

    @Override
    public void onItemClick(int position) {
        showWebViewFragment(mMedConditionList.get(position).getName());
    }
}