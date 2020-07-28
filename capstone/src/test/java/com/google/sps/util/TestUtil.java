// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the 'License');
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an 'AS IS' BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.util;

import com.google.gson.JsonParser;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.mockito.Mockito;

/** Assert that a certain response code was raised, not caring about what the associated text is. */
public class TestUtil {
  public static void assertResponseWithArbitraryTextRaised(
      int targetResponse, HttpServletResponse response) throws IOException {
    Mockito.verify(response, Mockito.times(1))
        .sendError(Mockito.eq(targetResponse), Mockito.anyString());
  }

  /** Assert that two JSON strings specify the same JSON object */
  public static void assertSameJsonObject(String a, String b) {
    JsonParser parser = new JsonParser();
    Assert.assertEquals(parser.parse(a), parser.parse(b));
  }
}
