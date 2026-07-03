package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.CallRecord
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CallScribe", appName)
  }

  @Test
  fun `verify secure action item parser`() {
    val record = CallRecord(
      id = 1,
      clientProfileId = null,
      phoneNumber = "+1 555-0100",
      durationSeconds = 60,
      transcript = "Testing transcript text",
      summary = "Summarized",
      actionItemsRaw = "Item A||Item B||Item C",
      isEncrypted = true
    )
    val list = record.actionItemsList
    assertEquals(3, list.size)
    assertEquals("Item A", list[0])
    assertEquals("Item B", list[1])
    assertEquals("Item C", list[2])
  }
}
