package com.donnelly.steve.scshuffle.application

import io.reactivex.subjects.PublishSubject

class RxBus {
    private val subject: PublishSubject<Any> by lazy {PublishSubject.create<Any>()}

    fun setEvent(value: Any) {
        subject.onNext(value)
    }

    fun getEvents(): PublishSubject<Any> {
        return subject
    }
}