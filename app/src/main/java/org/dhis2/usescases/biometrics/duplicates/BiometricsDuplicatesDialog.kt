package org.dhis2.usescases.biometrics.duplicates

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import com.google.android.material.snackbar.Snackbar
import io.reactivex.functions.Consumer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.dhis2.R
import org.dhis2.bindings.app
import org.dhis2.commons.biometrics.BIOMETRICS_CONFIRM_IDENTITY_REQUEST
import org.dhis2.commons.data.SearchTeiModel
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.data.biometrics.BiometricsClient
import org.dhis2.data.biometrics.BiometricsClientFactory.get
import org.dhis2.data.biometrics.SimprintsItem
import org.dhis2.databinding.DialogBiometricsDuplicatesBinding
import org.dhis2.usescases.searchTrackEntity.ui.mapper.TEICardMapper
import org.dhis2.utils.LastSelection
import org.hisp.dhis.android.core.arch.call.D2Progress
import javax.inject.Inject

class BiometricsDuplicatesDialog : DialogFragment(), BiometricsDuplicatesDialogView {

    private var onEnrollNewListener: ((sessionId: String) -> Unit)? = null
    private var onOpenTeiDashboardListener: ((String, String, String) -> Unit)? = null
    private var onEnrollWithoutBiometricsListener: (() -> Unit)? = null
    private lateinit var binding: DialogBiometricsDuplicatesBinding
    private var lastSelection: LastSelection? = null

    @Inject
    lateinit var presenter: BiometricsDuplicatesDialogPresenter

    @Inject
    lateinit var teiCardMapper: TEICardMapper

    private lateinit var adapter: BiometricsDuplicatesDialogAdapter

    private var isDialogShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BiometricsConfirmationDialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        create(
            requireArguments().getString(TRACKED_ENTITY_TYPE_UID)!!,
            requireArguments().getString(PROGRAM_UID)!!
        )

        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.dialog_biometrics_duplicates,
            container,
            false
        )


        val possibleDuplicates: List<SimprintsItem> =
            requireArguments().getParcelableArrayList<SimprintsItemParcelable>(POSSIBLE_DUPLICATES)?.toList()?.map {
                SimprintsItem(it.guid, it.confidence)
            } ?: emptyList()


        val biometricsSessionId = requireArguments().getString(BIOMETRICS_SESSION_ID)!!
        val programUid = requireArguments().getString(PROGRAM_UID)!!
        val trackedEntityTypeUid = requireArguments().getString(TRACKED_ENTITY_TYPE_UID)!!
        val biometricsAttributeUid = requireArguments().getString(BIOMETRICS_ATTRIBUTE_UID)!!
        val enrollNewVisible = requireArguments().getBoolean(ENROL_NEW_VISIBLE)

        if (enrollNewVisible){
            binding.enrollNewButton.visibility = View.VISIBLE

        } else {
            binding.enrollNewButton.visibility = View.GONE
        }

        binding.enrollNewButton.setOnClickListener {
            presenter.enrollNewClick()
        }

        binding.enrollWithoutBiometricsButton.setOnClickListener {
            presenter.enrollWithoutBiometrics()
        }

        this.adapter =
            BiometricsDuplicatesDialogAdapter(teiCardMapper, ColorUtils()) { searchTeiModel ->
                presenter.onTEIClick(
                    searchTeiModel.tei.uid(),
                    searchTeiModel.selectedEnrollment.uid(),
                    searchTeiModel.isOnline
                )
            }

        binding.duplicatesRecycler.adapter = adapter

        presenter.init(
            this,
            possibleDuplicates,
            biometricsSessionId,
            programUid,
            trackedEntityTypeUid,
            biometricsAttributeUid
        )

        return binding.root
    }

    override fun onCancel(dialog: DialogInterface) {
        presenter.onDetach()
        super.onCancel(dialog)
    }

    override fun setLiveData(flow: Flow<PagingData<SearchTeiModel>>) {
        lifecycleScope.launch {
            flow.collectLatest {
                adapter.addOnPagesUpdatedListener {
                    if (adapter.snapshot().items.isEmpty()) {
                        binding.duplicatesEmptyContainer.visibility = View.VISIBLE
                        binding.duplicatesRecycler.visibility = View.GONE
                    } else {
                        binding.duplicatesEmptyContainer.visibility = View.GONE
                        binding.duplicatesRecycler.visibility = View.VISIBLE
                    }
                }

                adapter.submitData(it)
            }
        }
    }

    override fun openDashboard(teiUid: String, programUid: String, enrollmentUid: String) {
        onOpenTeiDashboardListener?.invoke(teiUid, programUid, enrollmentUid)
    }

    override fun show(manager: FragmentManager, tag: String?) {
        isDialogShown = true
        super.show(manager, tag)
    }

    override fun dismiss() {
        presenter.onDetach()
        if (isDialogShown) {
            isDialogShown = false
            super.dismiss()
        }
    }

    override fun onStop() {
        presenter.onDetach()
        super.onStop()
    }

    override fun downloadProgress(): Consumer<D2Progress> {
        return Consumer {
            Snackbar.make(
                binding.root,
                getString(R.string.downloading),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    override fun couldNotDownload(typeName: String) {
        Toast.makeText(
            context,
            getString(R.string.download_tei_error, typeName),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun enrollNew(biometricsSessionId: String) {
        onEnrollNewListener?.invoke(biometricsSessionId)
        dismiss()
    }

    override fun enrollWithoutBiometrics() {
        onEnrollWithoutBiometricsListener?.invoke()
        dismiss()
    }

    override fun sendBiometricsConfirmIdentity(
        sessionId: String,
        guid: String,
        teiUid: String,
        enrollmentUid: String,
        isOnline: Boolean
    ) {
        lastSelection = LastSelection(teiUid, enrollmentUid, isOnline)

        val extras =
            mutableMapOf(BiometricsClient.SIMPRINTS_TRACKED_ENTITY_INSTANCE_ID to teiUid)

        context?.let { get(it).confirmIdentify(this, sessionId, guid, extras) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            BIOMETRICS_CONFIRM_IDENTITY_REQUEST -> {
                if (lastSelection != null) {
                    presenter.onTEIClick(
                        lastSelection!!.teiUid, lastSelection!!.enrollmentUid,
                        lastSelection!!.isOnline
                    )
                    lastSelection = null
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun setOnOpenTeiDashboardListener(onOpenTeiDashboardListener: (teiUid: String, programUid: String, enrollmentUid: String) -> Unit) {
        this.onOpenTeiDashboardListener = onOpenTeiDashboardListener
    }

    fun setOnEnrollNewListener(onEnrollNewListener: (sessionId: String) -> Unit) {
        this.onEnrollNewListener = onEnrollNewListener
    }

    fun setOnEnrollWithoutBiometricsListener(onEnrollWithoutBiometricsListener: () -> Unit) {
        this.onEnrollWithoutBiometricsListener = onEnrollWithoutBiometricsListener
    }

    private fun create(teiType: String, program: String) {
        app()
            .userComponent()!!
            .plus(BiometricsDuplicatesDialogModule(requireContext(), teiType, program))
            .inject(this)
    }

    companion object {
        private const val POSSIBLE_DUPLICATES = "POSSIBLE_DUPLICATES"
        private const val BIOMETRICS_SESSION_ID = "BIOMETRICS_SESSION_ID"
        private const val PROGRAM_UID = "PROGRAM_UID"
        private const val TRACKED_ENTITY_TYPE_UID = "TRACKED_ENTITY_TYPE_UID"
        private const val BIOMETRICS_ATTRIBUTE_UID = "BIOMETRICS_ATTRIBUTE_UID"
        private const val ENROL_NEW_VISIBLE = "ENROL_NEW_VISIBLE"

        val TAG: String = this::class.java.name

        @JvmStatic
        fun newInstance(
            possibleDuplicates: List<SimprintsItem>,
            sessionId: String,
            programUid: String,
            trackedEntityTypeUid: String,
            biometricsAttributeUid: String,
            enrollNewVisible: Boolean
        ): BiometricsDuplicatesDialog {
            val fragment = BiometricsDuplicatesDialog()

            val args = Bundle()

            val possibleDuplicatesParcelable = possibleDuplicates.map {
                SimprintsItemParcelable(it.guid, it.confidence)
            }

            args.putParcelableArrayList(POSSIBLE_DUPLICATES, ArrayList(possibleDuplicatesParcelable))
            args.putString(BIOMETRICS_SESSION_ID, sessionId)
            args.putString(PROGRAM_UID, programUid)
            args.putString(TRACKED_ENTITY_TYPE_UID, trackedEntityTypeUid)
            args.putString(BIOMETRICS_ATTRIBUTE_UID, biometricsAttributeUid)
            args.putBoolean(ENROL_NEW_VISIBLE, enrollNewVisible)
            fragment.arguments = args

            return fragment
        }
    }
}

data class SimprintsItemParcelable(
    val guid: String,
    val confidence: Float
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readFloat()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(guid)
        parcel.writeFloat(confidence)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SimprintsItemParcelable> {
        override fun createFromParcel(parcel: Parcel): SimprintsItemParcelable {
            return SimprintsItemParcelable(parcel)
        }

        override fun newArray(size: Int): Array<SimprintsItemParcelable?> {
            return arrayOfNulls(size)
        }
    }
}

inline fun <reified T : Parcelable> Intent.extractParcelableArrayListExtra(key: String): List<T>? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelableArrayListExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
}
