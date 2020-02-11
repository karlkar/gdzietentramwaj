package com.kksionek.gdzietentramwaj.base.view

import android.widget.ImageView
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import org.junit.Test

class PicassoImageLoaderTest {

    private val uri = "uri"
    private val requestCreator: RequestCreator = mock()
    private val picasso: Picasso = mock {
        on { load(uri) } doReturn requestCreator
    }

    private val tested = PicassoImageLoader(picasso)

    @Test
    fun `should load image into imageView when requested`() {
        // given
        val imageView: ImageView = mock()

        // when
        tested.load(uri, imageView)

        // then
        verify(requestCreator).into(imageView)
    }
}