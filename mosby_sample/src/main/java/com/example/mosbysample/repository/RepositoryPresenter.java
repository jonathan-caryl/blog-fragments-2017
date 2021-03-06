package com.example.mosbysample.repository;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.example.common.network.GitHubAPI;
import com.example.common.network.models.Repository;
import com.example.common.ui.ProgressBarProvider;
import com.example.common.ui.adapters.RepositoryAdapter;
import com.example.mosbysample.BuildConfig;
import com.example.mosbysample.ContentActivity;
import com.example.mosbysample.R;
import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by myotive on 2/14/2017.
 */

public class RepositoryPresenter extends MvpBasePresenter<RepositoryContract.View>
        implements RepositoryContract.Presenter{

    public static final String TAG = RepositoryPresenter.class.getSimpleName();

    private GitHubAPI gitHubAPI;
    private ProgressBarProvider progressBarProvider;

    private List<Repository> repositories;

    public RepositoryPresenter(GitHubAPI gitHubAPI, ProgressBarProvider progressBarProvider){
        this.gitHubAPI = gitHubAPI;
        this.progressBarProvider = progressBarProvider;
    }

    @Override
    public void attachView(RepositoryContract.View view) {
        super.attachView(view);

        view.setOnRepositoryItemClick(new RepositoryAdapter.RepositoryItemClick() {
            @Override
            public void OnRepositoryItemClick(View view, Repository item) {

                Activity activity = (Activity)view.getContext();

                Intent intent = new Intent(activity, ContentActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(ContentActivity.REPOSITORY_EXTRA, item);
                activity.startActivity(intent);
                activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        // To prevent getting rate limited, I'm going to cache the repositories here
        if(repositories == null) {
            getRepositories();
        }
    }

    @Override
    public void getRepositories() {
        this.progressBarProvider.showProgressBar();

        gitHubAPI.GetRepos(BuildConfig.GITHUB_OWNER).enqueue(new Callback<List<Repository>>() {
            @Override
            public void onResponse(Call<List<Repository>> call, Response<List<Repository>> response) {
                if(response.isSuccessful() && isViewAttached()){
                    repositories = response.body();
                    getView().updateRepositoryList(repositories);
                }

                progressBarProvider.hideProgressBar();
            }

            @Override
            public void onFailure(Call<List<Repository>> call, Throwable t) {
                Log.e(TAG, "Error calling GetRepos", t);

                progressBarProvider.hideProgressBar();
            }
        });
    }
}
