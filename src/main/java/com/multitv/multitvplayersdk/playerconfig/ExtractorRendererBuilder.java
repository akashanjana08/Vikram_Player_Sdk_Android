/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.multitv.multitvplayersdk.playerconfig;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.net.Uri;
import android.os.Handler;

import com.google.android.exoplayer.multitv.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.multitv.MediaCodecSelector;
import com.google.android.exoplayer.multitv.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.multitv.TrackRenderer;
import com.google.android.exoplayer.multitv.audio.AudioCapabilities;
import com.google.android.exoplayer.multitv.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.multitv.text.TextTrackRenderer;
import com.google.android.exoplayer.multitv.upstream.Allocator;
import com.google.android.exoplayer.multitv.upstream.DataSource;
import com.google.android.exoplayer.multitv.upstream.DefaultAllocator;
import com.google.android.exoplayer.multitv.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.multitv.upstream.DefaultUriDataSource;

public class ExtractorRendererBuilder implements MediaPlayer.RendererBuilder {

  private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
  private static final int BUFFER_SEGMENT_COUNT = 256;

  private final Context context;
  private final String userAgent;
  private final Uri uri;

  public ExtractorRendererBuilder(Context context, String userAgent, Uri uri) {
    this.context = context;
    this.userAgent = userAgent;
    this.uri = uri;
  }

  @Override
  public void buildRenderers(MediaPlayer player) {
    Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);
    Handler mainHandler = player.getMainHandler();

    // Build the video and audio renderers.
    DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter(mainHandler, null);
    DataSource dataSource = new DefaultUriDataSource(context, bandwidthMeter, userAgent);
    ExtractorSampleSource sampleSource = new ExtractorSampleSource(uri, dataSource, allocator,
        BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE, mainHandler, player, 0);
    MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(context,
        sampleSource, MediaCodecSelector.DEFAULT, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 5000,
        mainHandler, player, 50);
    MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource,
        MediaCodecSelector.DEFAULT, null, true, mainHandler, player,
        AudioCapabilities.getCapabilities(context), AudioManager.STREAM_MUSIC);
    TrackRenderer textRenderer = new TextTrackRenderer(sampleSource, player,
        mainHandler.getLooper());

    // Invoke the callback.
    TrackRenderer[] renderers = new TrackRenderer[MediaPlayer.RENDERER_COUNT];
    renderers[MediaPlayer.TYPE_VIDEO] = videoRenderer;
    renderers[MediaPlayer.TYPE_AUDIO] = audioRenderer;
    renderers[MediaPlayer.TYPE_TEXT] = textRenderer;
    player.onRenderers(renderers, bandwidthMeter);
  }

  @Override
  public void cancel() {
    // Do nothing.
  }

}
