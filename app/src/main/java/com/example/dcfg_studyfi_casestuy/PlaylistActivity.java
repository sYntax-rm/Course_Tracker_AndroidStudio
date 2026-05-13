package com.example.dcfg_studyfi_casestuy;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PlaylistActivity — Study music player
 *
 * Audio  : res/raw MP3 files bundled in the app (offline, no internet needed)
 * Data   : SQLite via existing DatabaseHelper — tracks table added via DatabaseHelper_ADDITIONS
 *
 * Features:
 *  - Play / Pause / Next / Previous
 *  - Loop toggle  (single-track repeat)
 *  - Shuffle toggle
 *  - SeekBar with live time display (polls every 500ms)
 *  - Category chip filter: All / Lofi / Chill / Nature
 *  - Auto-advance to next track on completion
 *  - Highlighted currently-playing row in ListView
 */
public class PlaylistActivity extends AppCompatActivity {

    // ── DB ─────────────────────────────────────────────────────────────────
    private DatabaseHelper dbHelper;

    // ── Track state ────────────────────────────────────────────────────────
    private List<TrackModel> allTracks      = new ArrayList<>();
    private List<TrackModel> filteredTracks = new ArrayList<>();
    private int     currentIndex    = -1;
    private String  currentCategory = "All";
    private boolean isLooping       = false;
    private boolean isShuffled      = false;

    // ── MediaPlayer ────────────────────────────────────────────────────────
    private MediaPlayer mediaPlayer;

    // ── SeekBar Handler ────────────────────────────────────────────────────
    private final Handler  seekHandler = new Handler(Looper.getMainLooper());
    private Runnable seekRunnable;

    // ── Views ──────────────────────────────────────────────────────────────
    private TextView    tvCurrentTrackTitle, tvCurrentTrackArtist;
    private TextView    tvCurrentTime, tvTotalTime, tvAlbumEmoji;
    private ImageButton btnPlayPause, btnNext, btnPrevious;
    private ImageButton btnLoopToggle, btnShuffleToggle;
    private SeekBar     seekBarProgress;
    private ListView    lvTracks;
    private TextView    chipAll, chipLofi, chipChill, chipNature;
    private TrackAdapter trackAdapter;

    // ══════════════════════════════════════════════════════════════════════
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        dbHelper = new DatabaseHelper(this);
        bindViews();
        seedTracksIfEmpty();
        loadTracks("All");
        setupCategoryChips();
        setupPlaybackControls();
        setupSeekBar();
    }

    // ── Bind views ─────────────────────────────────────────────────────────
    private void bindViews() {

        tvCurrentTrackTitle  = findViewById(R.id.tvCurrentTrackTitle);
        tvCurrentTrackArtist = findViewById(R.id.tvCurrentTrackArtist);
        tvCurrentTime        = findViewById(R.id.tvCurrentTime);
        tvTotalTime          = findViewById(R.id.tvTotalTime);
        tvAlbumEmoji         = findViewById(R.id.tvAlbumEmoji);
        btnPlayPause         = findViewById(R.id.btnPlayPause);
        btnNext              = findViewById(R.id.btnNext);
        btnPrevious          = findViewById(R.id.btnPrevious);
        btnLoopToggle        = findViewById(R.id.btnLoopToggle);
        btnShuffleToggle     = findViewById(R.id.btnShuffleToggle);
        seekBarProgress      = findViewById(R.id.seekBarProgress);
        lvTracks             = findViewById(R.id.lvTracks);
        chipAll              = findViewById(R.id.chipAll);
        chipLofi             = findViewById(R.id.chipLofi);
        chipChill            = findViewById(R.id.chipChill);
        chipNature           = findViewById(R.id.chipNature);
    }

    // ── Load & filter tracks ───────────────────────────────────────────────
    private void loadTracks(String category) {
        allTracks      = dbHelper.getAllTracks();
        filteredTracks = filterByCategory(allTracks, category);
        trackAdapter   = new TrackAdapter(filteredTracks);
        lvTracks.setAdapter(trackAdapter);
        lvTracks.setOnItemClickListener((p, v, pos, id) -> {
            currentIndex = pos;
            playTrackAt(currentIndex);
        });
    }

    private List<TrackModel> filterByCategory(List<TrackModel> src, String cat) {
        if (cat.equals("All")) return new ArrayList<>(src);
        List<TrackModel> out = new ArrayList<>();
        for (TrackModel t : src)
            if (t.category.equalsIgnoreCase(cat)) out.add(t);
        return out;
    }

    // ── Category chips ─────────────────────────────────────────────────────
    private void setupCategoryChips() {
        View.OnClickListener click = v -> {
            resetChipStyles();
            TextView chip = (TextView) v;
            chip.setTextColor(0xFFFFFFFF);
            int id = v.getId();
            if      (id == R.id.chipLofi)   currentCategory = "lofi";
            else if (id == R.id.chipChill)  currentCategory = "chill";
            else if (id == R.id.chipNature) currentCategory = "nature";
            else                            currentCategory = "All";
            filteredTracks = filterByCategory(allTracks, currentCategory);
            trackAdapter.updateData(filteredTracks);
        };
        chipAll.setOnClickListener(click);
        chipLofi.setOnClickListener(click);
        chipChill.setOnClickListener(click);
        chipNature.setOnClickListener(click);
    }

    private void resetChipStyles() {
        for (TextView c : new TextView[]{chipAll, chipLofi, chipChill, chipNature}) {
            c.setTextColor(0xFF8A8FA8);
        }
    }

    // ── Playback controls ──────────────────────────────────────────────────
    private void setupPlaybackControls() {

        btnPlayPause.setOnClickListener(v -> {
            if (mediaPlayer == null) {
                if (!filteredTracks.isEmpty()) { currentIndex = 0; playTrackAt(0); }
                return;
            }
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
            } else {
                mediaPlayer.start();
                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                startSeekUpdater();
            }
        });

        btnNext.setOnClickListener(v -> playNext());

        btnPrevious.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.getCurrentPosition() > 3000)
                mediaPlayer.seekTo(0);
            else
                playPrevious();
        });

        btnLoopToggle.setOnClickListener(v -> {
            isLooping = !isLooping;
            if (mediaPlayer != null) mediaPlayer.setLooping(isLooping);
            btnLoopToggle.setColorFilter(isLooping ? 0xFF6C8EFF : 0xFF8A8FA8);
            Toast.makeText(this, isLooping ? "Loop ON" : "Loop OFF", Toast.LENGTH_SHORT).show();
        });

        btnShuffleToggle.setOnClickListener(v -> {
            isShuffled = !isShuffled;
            if (isShuffled) Collections.shuffle(filteredTracks);
            else            filteredTracks = filterByCategory(allTracks, currentCategory);
            trackAdapter.updateData(filteredTracks);
            btnShuffleToggle.setColorFilter(isShuffled ? 0xFF6C8EFF : 0xFF8A8FA8);
            Toast.makeText(this, isShuffled ? "Shuffle ON" : "Shuffle OFF", Toast.LENGTH_SHORT).show();
        });
    }

    // ── Core playTrackAt

    private void playTrackAt(int index) {
        if (filteredTracks.isEmpty() || index < 0 || index >= filteredTracks.size()) return;
        TrackModel track = filteredTracks.get(index);
        releasePlayer();

        int resId = getAudioRes(track.resRawName);
        if (resId == 0) {
            Toast.makeText(this,
                    "Missing file: res/raw/" + track.resRawName + ".mp3", Toast.LENGTH_LONG).show();
            return;
        }

        mediaPlayer = MediaPlayer.create(this, resId);
        if (mediaPlayer == null) {
            Toast.makeText(this, "Could not load track", Toast.LENGTH_SHORT).show();
            return;
        }

        mediaPlayer.setLooping(isLooping);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(mp -> { if (!isLooping) playNext(); });

        tvCurrentTrackTitle .setText(track.title);
        tvCurrentTrackArtist.setText(track.artist);
        tvAlbumEmoji        .setText(emojiFor(track.category));
        btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        seekBarProgress.setMax(mediaPlayer.getDuration());
        tvTotalTime.setText(formatTime(mediaPlayer.getDuration()));

        startSeekUpdater();
        trackAdapter.setPlayingIndex(index);
        trackAdapter.notifyDataSetChanged();
    }

    private int getAudioRes(String name) {
        switch (name) {
            case "soft_piano": return R.raw.soft_piano;
            case "chillhop_beats_lofi": return R.raw.chillhop_beats_lofi;
            case "midnight_study_lofi": return R.raw.midnight_study_lofi;
            case "forest_rain": return R.raw.forest_rain;
            case "ocean_waves": return R.raw.ocean_waves;
            case "rainy_day_lofi": return R.raw.rainy_day_lofi;
            default: return 0;
        }
    }

    private void playNext() {
        if (filteredTracks.isEmpty()) return;
        currentIndex = (currentIndex + 1) % filteredTracks.size();
        playTrackAt(currentIndex);
    }

    private void playPrevious() {
        if (filteredTracks.isEmpty()) return;
        currentIndex = (currentIndex - 1 + filteredTracks.size()) % filteredTracks.size();
        playTrackAt(currentIndex);
    }

    // ── SeekBar ────────────────────────────────────────────────────────────
    private void setupSeekBar() {
        seekBarProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    tvCurrentTime.setText(formatTime(progress));
                }
            }
            @Override public void onStartTrackingTouch(SeekBar sb) { stopSeekUpdater(); }
            @Override public void onStopTrackingTouch(SeekBar sb)  { startSeekUpdater(); }
        });
    }

    private void startSeekUpdater() {
        stopSeekUpdater();
        seekRunnable = new Runnable() {
            @Override public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int pos = mediaPlayer.getCurrentPosition();
                    seekBarProgress.setProgress(pos);
                    tvCurrentTime.setText(formatTime(pos));
                }
                seekHandler.postDelayed(this, 500);
            }
        };
        seekHandler.post(seekRunnable);
    }

    private void stopSeekUpdater() {
        if (seekRunnable != null) seekHandler.removeCallbacks(seekRunnable);
    }

    // ── Util ───────────────────────────────────────────────────────────────
    private String formatTime(int ms) {
        int s = ms / 1000;
        return String.format("%d:%02d", s / 60, s % 60);
    }

    private String emojiFor(String cat) {
        switch (cat.toLowerCase()) {
            case "lofi":   return "🎧";
            case "chill":  return "🌊";
            case "nature": return "🌿";
            default:       return "🎵";
        }
    }

    private void releasePlayer() {
        stopSeekUpdater();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // ── Seed sample data (first run only) ─────────────────────────────────
    private void seedTracksIfEmpty() {
        if (dbHelper.getTrackCount() > 0) return;
        // Replace resRawName values with your actual filenames in res/raw (no .mp3 extension)
        dbHelper.insertTrack("Rainy Day Lofi",  "ChillHop Music", "lofi",   "rainy_day_lofi",   140000);
        dbHelper.insertTrack("Midnight Study",   "Lofi Girl",      "lofi",   "midnight_study_lofi",   1140000);
        dbHelper.insertTrack("Coffee & Code",    "Chillhop Beats", "lofi",   "chillhop_beats_lofi",   207000);
        dbHelper.insertTrack("Ocean Waves",      "Nature Sounds",  "chill",  "ocean_waves",  212000);
        dbHelper.insertTrack("Soft Piano Chill", "Calm Vibes",     "chill",  "soft_piano",  236000);
        dbHelper.insertTrack("Forest Rain",      "Ambient World",  "nature", "forest_rain", 1899000);
    }

    // ── Lifecycle
    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
        if (dbHelper != null) dbHelper.close();
    }

    // TrackModel

    public static class TrackModel {
        public int    id;
        public String title, artist, category, resRawName;
        public int    durationMs;

        public TrackModel(int id, String title, String artist,
                          String category, String resRawName, int durationMs) {
            this.id = id; this.title = title; this.artist = artist;
            this.category = category; this.resRawName = resRawName;
            this.durationMs = durationMs;
        }
    }


    // TrackAdapter

    private class TrackAdapter extends ArrayAdapter<TrackModel> {
        private List<TrackModel> data;
        private int playingIndex = -1;

        TrackAdapter(List<TrackModel> data) {
            super(PlaylistActivity.this, R.layout.item_track, data);
            this.data = new ArrayList<>(data);
        }

        void updateData(List<TrackModel> d) {
            data = new ArrayList<>(d);
            clear(); addAll(d); notifyDataSetChanged();
        }

        void setPlayingIndex(int i) { playingIndex = i; }

        @Override
        public View getView(int pos, View cv, ViewGroup parent) {
            if (cv == null)
                cv = getLayoutInflater().inflate(R.layout.item_track, parent, false);

            TrackModel t = data.get(pos);
            boolean playing = (pos == playingIndex);

            TextView tvNum = cv.findViewById(R.id.tvTrackNumber);
            TextView tvInd = cv.findViewById(R.id.tvPlayingIndicator);
            tvNum.setVisibility(playing ? View.GONE    : View.VISIBLE);
            tvInd.setVisibility(playing ? View.VISIBLE : View.GONE);
            tvNum.setText(String.valueOf(pos + 1));

            ((TextView) cv.findViewById(R.id.tvTrackTitle))   .setText(t.title);
            ((TextView) cv.findViewById(R.id.tvTrackArtist))  .setText(t.artist);
            ((TextView) cv.findViewById(R.id.tvTrackCategory)).setText(t.category);
            ((TextView) cv.findViewById(R.id.tvTrackDuration)).setText(formatTime(t.durationMs));


            return cv;
        }
    }
}
