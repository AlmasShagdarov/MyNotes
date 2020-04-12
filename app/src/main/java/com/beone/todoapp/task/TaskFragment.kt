package com.beone.todoapp.task

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionInflater
import com.beone.todoapp.R
import com.beone.todoapp.databinding.FragmentTaskBinding
import com.beone.todoapp.util.*


class TaskFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private var colorInt = 0
    private lateinit var binding: FragmentTaskBinding
    private lateinit var spinAdapter: ListExampleAdapter
    private val args: TaskFragmentArgs by navArgs()
    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(args.taskKey, requireActivity().application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTaskBinding.inflate(inflater, container, false)
        spinAdapter = ListExampleAdapter(
            requireActivity(),
            colorArray
        )
        initBinding()
        getInitialTaskContent()
        observeNavigateToList()
        return binding.root
    }

    private fun initBinding() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            spinner.adapter = spinAdapter
            spinner.onItemSelectedListener = this@TaskFragment
            newNodeTitle.text = when(args.taskTitle.formatHTMLtoEditable().toString().isEmpty()){
                true -> getString(R.string.new_note)
                false -> args.taskTitle.formatHTMLtoEditable().toString()
            }
            floatingActionButton.setOnClickListener {
                hideKeyboard()
                val txt = binding.edittext.text
                val txtSpanned = txt.formatEditableToHTML()
                taskViewModel.updateTask(txt.toString(), txtSpanned, colorInt)
            }
            btnBold.setOnClickListener {
                setSpan(Span.BOLD)
            }
            btnItalic.setOnClickListener {
                setSpan(Span.ITALIC)
            }
            btnUnderlined.setOnClickListener {
                setSpan(Span.UNDERLINE)
            }
            btnStrikeThrough.setOnClickListener {
                setSpan(Span.STRIKE)
            }
            btnListBullet.setOnClickListener {
                setSpan(Span.BULLETPOINT)
            }

            backButton.setOnClickListener {
                hideKeyboard()
                findNavController().navigateUp()
            }
        }

    }

    private fun getInitialTaskContent() {
        taskViewModel.task.observe(viewLifecycleOwner, Observer {
            binding.edittext.setText(it.taskContent.formatHTMLtoEditable())
            binding.spinner.setSelection(it.taskColor)
        })
    }

    private fun observeNavigateToList() {
        taskViewModel.navigateToList.observe(viewLifecycleOwner, Observer {
            if (it)
                findNavController().navigateUp()
        })
    }

    private fun setBulletPointSpan() {
        val text = binding.edittext.text.formatEditableToHTML().replace("<br>", "`<br>`")
        val splitText = text.split("`")
        val txt = "</p>".length
        Log.d("L", "$txt")

        val strBuilder = StringBuilder()
        var exists = false
        for (i in splitText.listIterator()) {
            if (i.contains("&#8226;")) {
                exists = true
            }
        }

        when (exists) {
            true -> {
                for (i in splitText.listIterator()) {
                    strBuilder.append(i.replace("&#8226; ", ""))
                }
            }
            false -> {
                for (i in splitText.listIterator()) {
                    if (i.isNotEmpty()) {
                        if (i.contains("<p dir=\"ltr\">")) {
                            if (!i.startsWith("<p dir=\"ltr\">") && i.contains("</p>")) {
                                strBuilder.append(
                                    "&#8226 " + i.replace(
                                        "<p dir=\"ltr\">",
                                        "<p dir=\"ltr\">&#8226 "
                                    )
                                )
                            }
                            if (i.startsWith("<p dir=\"ltr\">")) {
                                strBuilder.append(
                                    i.replace(
                                        "<p dir=\"ltr\">",
                                        "<p dir=\"ltr\">&#8226 "
                                    )
                                )
                            }
                        } else if (i.contains("<br>")) {
                            strBuilder.append(i)
                        } else if (i.trim().contains("</p>") && i.length == 6) {
                            strBuilder.append(i)
                        } else
                            strBuilder.append("&#8226 $i")
                    }
                    Log.d("Str", i)
                    Log.d("Length", "${i.length}~~")
                }
            }
        }

        binding.edittext.setText(strBuilder.toString().formatHTMLtoEditable())
    }

    private fun setTypeface(selectionStart: Int, selectionEnd: Int, typeface: Int) {
        val styleSpan = binding.edittext.text.getSpans(
            selectionStart,
            selectionEnd,
            StyleSpan::class.java
        )
        var styleExist = false
        for (i in styleSpan.indices) {
            if (styleSpan[i].style == typeface) {
                binding.edittext.text.removeSpan(styleSpan[i])
                styleExist = true
            }
        }
        if (!styleExist) {
            binding.edittext.text.setSpan(
                StyleSpan(typeface),
                selectionStart,
                selectionEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun setSpan(span: Span) {
        val selectionStart = binding.edittext.selectionStart
        val selectionEnd = binding.edittext.selectionEnd
        val start = when (selectionStart < selectionEnd) {
            false -> selectionEnd
            true -> selectionStart
        }
        val end = when (selectionStart < selectionEnd) {
            false -> selectionStart
            true -> selectionEnd
        }
        when (span) {
            Span.BOLD -> setTypeface(start, end, Typeface.BOLD)
            Span.ITALIC -> setTypeface(start, end, Typeface.ITALIC)
            Span.STRIKE -> setStrikeSpan(start, end)
            Span.UNDERLINE -> setUnderlineSpan(start, end)
            Span.BULLETPOINT -> setBulletPointSpan()
        }
    }

    private fun setUnderlineSpan(selectionStart: Int, selectionEnd: Int) {
        val text = binding.edittext.text
        val styleSpan = text.getSpans(
            selectionStart,
            selectionEnd,
            UnderlineSpan::class.java
        )
        var styleExist = false
        styleSpan.forEach {
            if (it is UnderlineSpan) {
                text.removeSpan(it)
                styleExist = true
            }
        }
        if (!styleExist) {
            text.setSpan(
                UnderlineSpan(),
                selectionStart,
                selectionEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun setStrikeSpan(selectionStart: Int, selectionEnd: Int) {
        val text = binding.edittext.text
        val styleSpan = text.getSpans(
            selectionStart,
            selectionEnd,
            StrikethroughSpan::class.java
        )
        var styleExist = false
        styleSpan.forEach {
            if (it is StrikethroughSpan) {
                text.removeSpan(it)
                styleExist = true
            }
        }
        if (!styleExist) {
            text.setSpan(
                StrikethroughSpan(),
                selectionStart,
                selectionEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        colorInt = position
    }
}