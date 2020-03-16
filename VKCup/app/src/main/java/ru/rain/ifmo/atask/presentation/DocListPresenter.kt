package ru.rain.ifmo.atask.presentation

import android.util.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import ru.rain.ifmo.atask.data.models.VKDoc
import ru.rain.ifmo.atask.domain.interactor.GetDocsInteractor
import ru.rain.ifmo.atask.domain.interactor.RenameDeleteInteractor
import ru.rain.ifmo.atask.domain.mvp.BasePresenter

class DocListPresenter(
    private val docsInteractor: GetDocsInteractor,
    private val renameDeleteInteractor: RenameDeleteInteractor
) : BasePresenter<DocListView>() {

    private var currentData = arrayListOf<VKDoc>()

    private var chosenIndex = -1

    private val compositeDisposable = CompositeDisposable()

    override var viewState: DocListView? = null

    override fun onAttach() {
        viewState?.showSpinner()
        if (currentData.isEmpty()) {
            requestDocs()
        }
        else {
            viewState?.addDocs(currentData)
            viewState?.hideSpinner()
            if (chosenIndex != -1)
                viewState?.scrollTo(chosenIndex)
            chosenIndex = -1
        }
    }

    override fun onDetach() {
        compositeDisposable.clear()
    }

    fun requestDocs(count: Int = 20, offset: Int = 0) {
        compositeDisposable.add(
            docsInteractor.getDocs(count, offset)
                .observeOn(Schedulers.io())
                .map { it.filter { doc -> !currentData.contains(doc) } }
                .map { currentData.addAll(it); it }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::arrivedDocs, {
                    Log.e(TAG, "Error occurred", it)
                }, {
                    Log.d(TAG, "Final data arrived")
                })
        )
    }

    private fun arrivedDocs(docs: List<VKDoc>) {
        viewState?.hideSpinner()
        viewState?.addDocs(docs)
    }

    fun rename(doc: VKDoc, newName: String) {
        doc.title = newName
        compositeDisposable.add(
            renameDeleteInteractor.renameDoc(doc)
                .subscribe({
                    Log.d("DocListTag", "Successfully renamed")
                }, {
                    Log.d("DocListTag", "Smth went wrong while renaming $it")
                })
        )
    }

    fun delete(doc: VKDoc) {
        var indexRemove = -1
        currentData.let {
            it.forEachIndexed { index, vkDoc ->
                if (vkDoc.id == doc.id) {
                    indexRemove = index
                    return@let
                }
            }
        }
        if (indexRemove >= 0) {
            viewState?.removeDoc(indexRemove)
            compositeDisposable.add(
                renameDeleteInteractor.deleteDoc(doc)
                    .subscribe({
                        Log.d("DocListTag", "Successfully deleted")
                    }, {
                        Log.d("DocListTag", "Smth went wrong while deleting $it")
                    })
            )
        }
    }

    fun chosenDoc(index: Int) {
        chosenIndex = index
    }

    companion object {
        private const val TAG = "DocListTag"
    }
}