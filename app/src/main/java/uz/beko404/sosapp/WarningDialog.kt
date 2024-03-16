package uz.beko404.sosapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import uz.beko404.sosapp.databinding.DialogWarningBinding

@SuppressLint("InflateParams")
class WarningDialog : DialogFragment() {
    var onClickListener: (() -> Unit)? = null
    private var _bn: DialogWarningBinding? = null
    private val bn get() = _bn!!

    private var message: String? = null
    private var dialogText: String? = null

    companion object {
        fun newInstance(): WarningDialog {
            val bundle = Bundle()
            val errorDialog = WarningDialog()
            errorDialog.arguments = bundle
            return errorDialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.CustomDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bn = DialogWarningBinding.inflate(inflater, container, false)
        return bn.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        bn.apply {
            okBtn.setOnClickListener {
                onClickListener?.invoke()
                dismiss()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _bn = null
    }
}