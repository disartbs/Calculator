package com.example.presentation.calculatorslist;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.App;
import com.example.calculatormain.R;
import com.example.data.repositories.CalculatorsListRepository;
import com.example.di.activity.calculatorslist.CalculatorsListModule;
import com.example.di.activity.calculatorslist.DaggerCalculatorsListComponent;
import com.example.models.CommonListItem;
import com.example.presentation.calculatorslist.adapters.CalculatorsListAdapter;
import com.example.presentation.calculatorslist.adapters.CalculatorsListAdapterImpl;
import com.example.presentation.calculatorslist.pagination.MainThreadExecutor;
import com.example.presentation.calculatorslist.pagination.MyPositionalDataSource;
import com.example.presentation.ui.bottomsheet.NewCalculatorBottomSheet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.LinkedList;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class CalculatorsListActivity extends AppCompatActivity
        implements CalculatorsListView {

    private static final String TAG = "CalculatorsList";

    @Inject
    NewCalculatorBottomSheet bottomSheetDialog;
    @Inject
    LinearLayoutManager manager;
    @Inject
    CalculatorsListAdapter adapter;
    @Inject
    CalculatorsListPresenter presenter;

    @Inject
    CalculatorsListRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculators_list);
        buildActivityComponentAndInject();
        setAdapter();
        presenter.attachView(this);
        initToolbar();
        initFAB();
    }

    private void initToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initFAB() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> bottomSheetDialog.show(getSupportFragmentManager(), ""));
    }

    private void buildActivityComponentAndInject() {
        DaggerCalculatorsListComponent.builder()
                .appComponent(App.getInstance().getAppComponent())
                .calculatorsListModule(new CalculatorsListModule(this))
                .build().inject(this);
    }

    private void setAdapter(){
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(manager);
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);
        MyPositionalDataSource dataSource = new MyPositionalDataSource(repository);
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPageSize(8)
                .build();
        PagedList<CommonListItem> pagedList = new PagedList.Builder<>(dataSource, config)
                .setNotifyExecutor(new MainThreadExecutor())
                .setFetchExecutor(Executors.newSingleThreadExecutor())
                .build();

        ((CalculatorsListAdapterImpl) adapter).submitList(pagedList);
        //  presenter.addCalculators();//Вынести сюда подписку, если надо
        recyclerView.setAdapter((CalculatorsListAdapterImpl) adapter);//Приведение такое надо?
    }

    @Override
    protected void onResume() {
        presenter.onResume();
        super.onResume();
    }

    @Override
    public void onCalculatorClick(int position) {
        presenter.onClickCalculator(position);
    }

    @Override
    public void onBottomSheetContinueClick(String name) {
        presenter.goToNewCalculator(name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_all: {
                presenter.onClickDeleteAll();
                break;
            }
            case R.id.update_all:{
                adapter.updateItems();
                break;
            }
        }
        return true;
    }
}