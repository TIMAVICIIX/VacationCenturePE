package com.example.vacationventurepe.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

open class BaseFragment : Fragment() {

    protected fun sendShortToast(context: Context,sendString: String) {
        Toast.makeText(context,sendString, Toast.LENGTH_SHORT).show()
    }

    protected fun sendLongToast(context: Context,sendString: String){
        Toast.makeText(context,sendString,Toast.LENGTH_LONG).show()
    }

}