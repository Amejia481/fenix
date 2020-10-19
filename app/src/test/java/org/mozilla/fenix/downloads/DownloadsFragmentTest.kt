/* This Source Code Form is subject to the terms of the Mozilla Public
   License, v. 2.0. If a copy of the MPL was not distributed with this
   file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.downloads

import android.content.Context
import android.os.Environment
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.FragmentActivity
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.android.synthetic.main.about_list_item.view.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.content.DownloadState
import mozilla.components.support.test.robolectric.testContext
import mozilla.components.support.test.rule.MainCoroutineRule
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.fenix.Config
import org.mozilla.fenix.R
import org.mozilla.fenix.ReleaseChannel
import org.mozilla.fenix.exceptions.ExceptionsAdapter
import org.mozilla.fenix.exceptions.viewholders.ExceptionsDeleteButtonViewHolder
import org.mozilla.fenix.exceptions.viewholders.ExceptionsHeaderViewHolder
import org.mozilla.fenix.exceptions.viewholders.ExceptionsListItemViewHolder
import org.mozilla.fenix.ext.components
import org.mozilla.fenix.helpers.FenixRobolectricTestRunner
import org.mozilla.fenix.library.downloads.DownloadFragment
import org.mozilla.fenix.library.downloads.DownloadFragmentStore
import org.mozilla.fenix.library.downloads.DownloadItem
import org.mozilla.fenix.utils.Settings
import org.robolectric.Robolectric
import java.io.File
import java.io.IOException

@Suppress("DEPRECATION")
@RunWith(FenixRobolectricTestRunner::class)
class DownloadsFragmentTest {

    @Test
    fun `downloads are sorted newest to oldest`() {

        val downloadedFile1 = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "1.pdf"
        )

        val downloadedFile2 = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "2.pdf"
        )

        downloadedFile1.createNewFile()
        downloadedFile2.createNewFile()


        val fragment = DownloadFragment()

        val expectedOrder = listOf(
            DownloadItem(id = "2","2.pdf",downloadedFile2.path,"null",null, DownloadState.Status.COMPLETED),
            DownloadItem(id = "1","1.pdf",downloadedFile1.path,"null",null, DownloadState.Status.COMPLETED),
        )

        val state: BrowserState = mockk(relaxed = true)

        every { state.downloads } returns mapOf(
            "1" to DownloadState(
                id = "1",
                createdTime = 1,
                url = "url",
                fileName = "1.pdf",
                status = DownloadState.Status.COMPLETED
            ),
            "2" to DownloadState(
                id = "2",
                createdTime = 2,
                url = "url",
                fileName = "2.pdf",
                status = DownloadState.Status.COMPLETED
            )
        )


        val list = fragment.provideDownloads(state)


        assertEquals(expectedOrder, list)

        downloadedFile1.delete()
        downloadedFile2.delete()
    }


}
