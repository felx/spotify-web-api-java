package com.wrapper.spotify.methods;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.wrapper.spotify.exceptions.BadRequestException;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import com.wrapper.spotify.Api;
import com.wrapper.spotify.HttpManager;
import com.wrapper.spotify.TestConfiguration;
import com.wrapper.spotify.TestUtil;
import com.wrapper.spotify.models.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.TestCase.fail;

@RunWith(MockitoJUnitRunner.class)
public class AlbumsRequestTest {

  @Test
  public void shouldGetAlbumResultForIds_async() throws Exception {
    final Api api = Api.DEFAULT_API;

    final AlbumsRequest.Builder requestBuilder = api.getAlbums("41MnTivkwTO3UUJ8DrqEJJ");
    if (TestConfiguration.USE_MOCK_RESPONSES) {
      requestBuilder.httpManager(TestUtil.MockedHttpManager.returningJson("albums.json"));
    }
    final AlbumsRequest request = requestBuilder.build();

    final CountDownLatch asyncCompleted = new CountDownLatch(1);

    final SettableFuture<List<Album>> albumsFuture = request.getAsync();
    Futures.addCallback(albumsFuture, new FutureCallback<List<Album>>() {

      @Override
      public void onSuccess(List<Album> albums) {
        assertEquals(1, albums.size());

        Album firstAlbum = albums.get(0);
        assertEquals("41MnTivkwTO3UUJ8DrqEJJ", firstAlbum.getId());
        assertEquals(AlbumType.ALBUM, firstAlbum.getAlbumType());
        assertEquals("2013-11-08", firstAlbum.getReleaseDate());
        assertEquals("day", firstAlbum.getReleaseDatePrecision());

        List<SimpleArtist> artists = firstAlbum.getArtists();
        SimpleArtist firstArtist = artists.get(0);
        assertEquals("https://api.spotify.com/v1/artists/53A0W3U0s8diEn9RhXQhVz", firstArtist.getHref());
        assertEquals("53A0W3U0s8diEn9RhXQhVz", firstArtist.getId());

        Page<SimpleTrack> tracksPage = firstAlbum.getTracks();
        assertEquals("https://api.spotify.com/v1/albums/41MnTivkwTO3UUJ8DrqEJJ/tracks?offset=0&limit=50", tracksPage.getHref());
        assertEquals(0, tracksPage.getOffset());
        assertEquals(50, tracksPage.getLimit());
        assertEquals(38, tracksPage.getTotal());
        assertEquals("4r9PmSmbAOOWqaGWLf6M9Q", tracksPage.getItems().get(0).getId());

        asyncCompleted.countDown();
      }

      @Override
      public void onFailure(Throwable throwable) {
        fail();
      }
    });

    asyncCompleted.await(1, TimeUnit.SECONDS);
  }

  @Test
  public void shouldGetAlbumResultForIds_sync() throws Exception {
    final Api api = Api.DEFAULT_API;

    final AlbumsRequest.Builder requestBuilder = api.getAlbums("41MnTivkwTO3UUJ8DrqEJJ");
    if (TestConfiguration.USE_MOCK_RESPONSES) {
      requestBuilder.httpManager(TestUtil.MockedHttpManager.returningJson("albums.json"));
    }
    final AlbumsRequest request = requestBuilder.build();

    List<Album> albums = request.get();

    assertEquals(1, albums.size());

    Album firstAlbum = albums.get(0);
    assertEquals("41MnTivkwTO3UUJ8DrqEJJ", firstAlbum.getId());
    assertEquals(AlbumType.ALBUM, firstAlbum.getAlbumType());
    assertEquals("2013-11-08", firstAlbum.getReleaseDate());
    assertEquals("day", firstAlbum.getReleaseDatePrecision());

    List<SimpleArtist> artists = firstAlbum.getArtists();
    SimpleArtist firstArtist = artists.get(0);
    assertEquals("https://api.spotify.com/v1/artists/53A0W3U0s8diEn9RhXQhVz", firstArtist.getHref());
    assertEquals("53A0W3U0s8diEn9RhXQhVz", firstArtist.getId());

    Page<SimpleTrack> tracksPage = firstAlbum.getTracks();
    assertEquals("https://api.spotify.com/v1/albums/41MnTivkwTO3UUJ8DrqEJJ/tracks?offset=0&limit=50", tracksPage.getHref());
    assertEquals(0, tracksPage.getOffset());
    assertEquals(50, tracksPage.getLimit());
    assertEquals(38, tracksPage.getTotal());
    assertEquals("4r9PmSmbAOOWqaGWLf6M9Q", tracksPage.getItems().get(0).getId());
  }

  @Test
  public void shouldFailForNotFound_async() throws Exception {
    final Api api = Api.DEFAULT_API;

    final AlbumsRequest.Builder requestBuilder = api.getAlbums("idontexist");
    if (TestConfiguration.USE_MOCK_RESPONSES) {
      requestBuilder.httpManager(TestUtil.MockedHttpManager.returningJson("albums-none-found.json"));
    }
    final AlbumsRequest request = requestBuilder.build();

    final CountDownLatch asyncCompleted = new CountDownLatch(1);

    final SettableFuture<List<Album>> albumFuture = request.getAsync();

    Futures.addCallback(albumFuture, new FutureCallback<List<Album>>() {
      @Override
      public void onSuccess(List<Album> albums) {
        assertEquals(1, albums.size());
        assertNull(albums.get(0));
        asyncCompleted.countDown();
      }

      @Override
      public void onFailure(Throwable throwable) {
        fail();
      }

    });

    asyncCompleted.await(1, TimeUnit.SECONDS);
  }

}
