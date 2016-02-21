package com.joshromer.logout

import android.support.v7.app.AppCompatActivity

import android.os.Bundle

import android.view.View

import android.widget.EditText


import android.util.Log
import android.widget.Button
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
    "top.cs.rutgers.edu", "vi.cs.rutgers.edu", "assembly.cs.rutgers.edu", "cpp.cs.rutgers.edu",
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
    private var mEmailSignInButton: Button? = null
    private var mNetidView: EditText? = null
    private var mPasswordView: EditText? = null
    private var mProgressView: View? = null
    private var mLoginFormView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logout)
        // Set up the login form.
        mNetidView = findViewById(R.id.email) as EditText

        mPasswordView = findViewById(R.id.password) as EditText

        mEmailSignInButton = findViewById(R.id.email_sign_in_button) as Button

        mNetidChangeObservable = RxTextView.textChanges(mNetidView!!)
        mPasswordChangeObservable = RxTextView.textChanges(mPasswordView!!)

        onClick()

        combineLatestEvents()

        mLoginFormView = findViewById(R.id.login_form)
        mProgressView = findViewById(R.id.login_progress)

    }

    private fun combineLatestEvents() {
        mSubscription = Observable.combineLatest(mNetidChangeObservable, mPasswordChangeObservable,
                {a, b -> a.isNotEmpty() && b.isNotEmpty()})
                .subscribe {valid ->
                    if(valid){
                        mEmailSignInButton!!.isEnabled = true
                    } else {
                        mEmailSignInButton!!.isEnabled = false
                    }

                }
    }

    private fun onClick(){
        mOnClickSubscription = RxView.clicks(mEmailSignInButton!!)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                //set subscribeOn to io here to make sure clicks happens on main thread
                .flatMap { Observable.from(HOSTS).subscribeOn(Schedulers.io()) }
                .map { sshSignOut(it) }
                .onErrorReturn { it.toString() }
                .subscribe({
                    if (it !in HOSTS)
                        Log.d("Error found", it)
                    else
                        Log.d("Signout of", it)
                }, {
                    //Should never get here
                    Log.d(it.toString(), "Error found")
                }, {
                    Log.d("completed", "Logout")
                    onClick()
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

