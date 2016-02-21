package com.joshromer.logout

import android.support.v7.app.AppCompatActivity

import android.os.Bundle
import android.support.design.widget.Snackbar

import android.view.View

import android.widget.EditText


import android.util.Log
import android.widget.Button
import com.gc.materialdesign.views.ProgressBarDeterminate
import com.jakewharton.rxbinding.view.RxView
import com.jakewharton.rxbinding.widget.RxTextView
import com.jcraft.jsch.JSch
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.ByteArrayInputStream

/**
 * A login screen that offers login via email/password.
 */
class LogoutActivity : AppCompatActivity() {

    private var HOSTS = arrayOf("cd.cs.rutgers.edu","cp.cs.rutgers.edu", "grep.cs.rutgers.edu",
    "kill.cs.rutgers.edu", "less.cs.rutgers.edu", "ls.cs.rutgers.edu",
    "man.cs.rutgers.edu", "pwd.cs.rutgers.edu", "rm.cs.rutgers.edu",
    "top.cs.rutgers.edu", "vi.cs.rutgers.edu", "cpp.cs.rutgers.edu",
    "pascal.cs.rutgers.edu", "java.cs.rutgers.edu", "python.cs.rutgers.edu",
    "perl.cs.rutgers.edu", "lisp.cs.rutgers.edu", "basic.cs.rutgers.edu", "batch.cs.rutgers.edu",
    "prolog.cs.rutgers.edu", "assembly.cs.rutgers.edu", "adapter.cs.rutgers.edu",
    "builder.cs.rutgers.edu", "command.cs.rutgers.edu", "composite.cs.rutgers.edu",
    "decorator.cs.rutgers.edu", "design.cs.rutgers.edu", "facade.cs.rutgers.edu",
    "factory.cs.rutgers.edu", "flyweight.cs.rutgers.edu", "interpreter.cs.rutgers.edu",
    "mediator.cs.rutgers.edu", "null.cs.rutgers.edu", "patterns.cs.rutgers.edu",
    "prototype.cs.rutgers.edu", "singleton.cs.rutgers.edu","specification.cs.rutgers.edu",
    "state.cs.rutgers.edu", "strategy.cs.rutgers.edu", "template.cs.rutgers.edu",
    "utility.cs.rutgers.edu", "visitor.cs.rutgers.edu")

    private var mNetidChangeObservable: Observable<CharSequence>? = null
    private var mPasswordChangeObservable: Observable<CharSequence>? = null

    private var mOnClickSubscription: Subscription? = null
    private var mSubscription: Subscription? = null

    // UI references.
    private var mLogoutButton: Button? = null
    private var mNetidView: EditText? = null
    private var mPasswordView: EditText? = null
    private var mProgressView: ProgressBarDeterminate? = null
    private var mLogoutFormView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logout)
        mNetidView = findViewById(R.id.email) as EditText

        mPasswordView = findViewById(R.id.password) as EditText

        mLogoutButton = findViewById(R.id.email_sign_in_button) as Button

        mNetidChangeObservable = RxTextView.textChanges(mNetidView!!)
        mPasswordChangeObservable = RxTextView.textChanges(mPasswordView!!)

        onClickRx()

        combineLatestEvents()

        mLogoutFormView = findViewById(R.id.login_form)
        mProgressView = findViewById(R.id.logout_progress) as ProgressBarDeterminate
        mProgressView!!.setMax(HOSTS.size)

    }

    private fun combineLatestEvents() {
        mSubscription = Observable.combineLatest(mNetidChangeObservable, mPasswordChangeObservable,
                {a, b -> a.isNotEmpty() && b.isNotEmpty()})
                .subscribe {valid ->
                    if(valid){
                        mLogoutButton!!.isEnabled = true
                    } else {
                        mLogoutButton!!.isEnabled = false
                    }

                }
    }

    private fun onClickRx(){
        mOnClickSubscription = RxView.clicks(mLogoutButton!!)
                //set subscribeOn to io here to make sure clicks happens on main thread
                .flatMap { Observable.from(HOSTS).subscribeOn(Schedulers.io())}
                .map { sshSignOut(it) }
                .onErrorReturn { it.toString() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it !in HOSTS) {
                        Log.d("Error found", it)
                        //TODO update for other errorsj
                        Snackbar.make(mLogoutFormView!!, "Authentication Failed", Snackbar.LENGTH_LONG).show()
                        mProgressView!!.progress = 0
                    }
                    else {
                        Log.d("Signout of ${HOSTS.indexOf(it)}", it)
                        mProgressView!!.progress = HOSTS.indexOf(it) + 1
                    }

                }, {
                    //Should never get here
                    Log.d(it.toString(), "Error found")
                }, {
                    Log.d("completed", "Logout")
                    onClickRx()
                })
    }

    private fun sshSignOut(host: String): String {

        val jsch = JSch()
        val user = mNetidView!!.text.toString()
        val password = mPasswordView!!.text.toString()

        val session = jsch.getSession(user, host, 22)
        session.setConfig("StrictHostKeyChecking", "no")
        session.setPassword(password)

        session.connect(30000)

        val channel = session.openChannel("shell")
        val input = ByteArrayInputStream("killall -u $user \n".toByteArray())
        channel.inputStream = input

        channel.connect(30000)

        return host

    }

}

