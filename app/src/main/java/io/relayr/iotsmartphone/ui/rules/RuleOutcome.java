package io.relayr.iotsmartphone.ui.rules;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import io.relayr.iotsmartphone.R;
import io.relayr.iotsmartphone.storage.Constants;
import io.relayr.iotsmartphone.storage.Storage;
import io.relayr.java.model.models.transport.DeviceCommand;

import static io.relayr.iotsmartphone.storage.Constants.DeviceType.PHONE;

public class RuleOutcome extends LinearLayout {

    @InjectView(R.id.rule_widget_color) View mColorView;

    @InjectView(R.id.rule_widget_icon) ImageView mIcon;
    @InjectView(R.id.rule_widget_empty_text) TextView mEmptyTv;
    @InjectView(R.id.rule_widget_container) View mContainer;

    @InjectView(R.id.rule_widget_meaning) TextView mMeaningTv;
    @InjectView(R.id.rule_widget_value) SwitchCompat mValueSwitch;

    private int mColor;

    private Boolean mValue = false;
    private DeviceCommand mCommand;
    private Constants.DeviceType mType;
    private FragmentRules.OutcomeListener mListener;

    public RuleOutcome(Context context) {
        this(context, null);
    }

    public RuleOutcome(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RuleOutcome(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setUp(int color, RuleBuilder rule, int position, FragmentRules.OutcomeListener listener) {
        mColor = color;
        mListener = listener;

        if (rule == null) return;

        mValue = rule.getOutcomeValue(position);
        if (mValue == null) return;

        mType = PHONE;
        for (DeviceCommand cmd : Storage.instance().loadCommands(mType))
            if (cmd.getName().equals(rule.getOutcomeName(position)))
                mCommand = cmd;

        if (isShown()) setOutcomeValues();
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ButterKnife.inject(this, this);

        mColorView.setBackgroundResource(mColor);

        if (mType != null) setOutcomeValues();
    }

    @SuppressWarnings("unused") @OnClick(R.id.rule_widget_remove_btn)
    public void onRemoveClicked() {
        mType = null;
        mCommand = null;
        toggleControls(false);
        mListener.removeOutcome();
    }

    @SuppressWarnings("unused") @OnClick(R.id.rule_widget_icon)
    public void onIconClicked() {
        final ConditionDialog view = (ConditionDialog) View.inflate(getContext(), R.layout.condition_dialog, null);
        view.setUp(mType, mCommand, false);

        new AlertDialog.Builder(getContext(), R.style.AppTheme_DialogOverlay)
                .setView(view)
                .setTitle(getResources().getString(R.string.cloud_device_dialog_title))
                .setNegativeButton(getResources().getString(R.string.close), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(getResources().getString(R.string.save), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if ((mType == null || mCommand == null) || !mCommand.equals(view.getSelected()) || mType != view.getType()) {
                            mCommand = (DeviceCommand) view.getSelected();
                            mType = view.getType();
                            mValue = true;
                            toggleControls(true);
                            setOutcomeValues();
                            mListener.outcomeChanged(mCommand, mValue);
                        }
                    }
                })
                .create()
                .show();
    }

    private void toggleControls(boolean show) {
        mEmptyTv.setVisibility(show ? GONE : VISIBLE);
        mContainer.setVisibility(show ? VISIBLE : GONE);
        mIcon.setImageResource(show ? (mType == null || mType == PHONE ? R.drawable.ic_graphic_phone :
                R.drawable.ic_graphic_watch) : R.drawable.ic_add_dark);
    }

    private void setOutcomeValues() {
        toggleControls(true);
        mMeaningTv.setText(mCommand.getName());

        mValueSwitch.setOnCheckedChangeListener(null);
        if (mValue != null) mValueSwitch.setChecked(mValue);
        mValueSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mValue = isChecked;
                mListener.outcomeChanged(mCommand, mValue);
            }
        });
    }
}