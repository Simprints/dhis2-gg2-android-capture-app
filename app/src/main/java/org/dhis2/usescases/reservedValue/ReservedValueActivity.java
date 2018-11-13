package org.dhis2.usescases.reservedValue;

import android.databinding.DataBindingUtil;
import android.os.Bundle;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.databinding.ActivityReservedValueBinding;

import java.util.List;

import javax.inject.Inject;

public class ReservedValueActivity extends ActivityGlobalAbstract implements ReservedValueContracts.View {

    private ActivityReservedValueBinding reservedBinding;
    private ReservedValueAdapter adapter;
    @Inject
    ReservedValueContracts.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new ReservedValueModule(this)).inject(this);
        super.onCreate(savedInstanceState);

        reservedBinding = DataBindingUtil.setContentView(this, R.layout.activity_reserved_value);
        adapter = new ReservedValueAdapter(presenter);
    }

    @Override
    public void setDataElements(List<ReservedValueModel> reservedValueModels) {
        if (reservedBinding.recycler.getAdapter() == null) {
            reservedBinding.recycler.setAdapter(adapter);
        }
        adapter.setDataElements(reservedValueModels);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this);
    }
}
