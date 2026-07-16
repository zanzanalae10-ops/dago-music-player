package com.dagomusic.core.equalizer

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import com.dagomusic.core.settings.DagoDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioEffectManager @Inject constructor(
    private val dataStore: DagoDataStore
) {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    private val scope = CoroutineScope(Dispatchers.Default)

    fun initEffects(audioSessionId: Int) {
        if (audioSessionId == 0) return
        try {
            releaseEffects()

            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = true
            }

            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = true
            }

            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = true
            }

            loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                enabled = true
            }

            // Restore last active equalizer settings from dataStore
            scope.launch {
                val eqEnabled = dataStore.equalizerEnabled.first()
                val bass = dataStore.equalizerBassBoost.first()
                val virt = dataStore.equalizerVirtualizer.first()
                val loud = dataStore.equalizerLoudness.first()
                val b1 = dataStore.equalizerBand1.first()
                val b2 = dataStore.equalizerBand2.first()
                val b3 = dataStore.equalizerBand3.first()
                val b4 = dataStore.equalizerBand4.first()
                val b5 = dataStore.equalizerBand5.first()

                setEffectsEnabled(eqEnabled)
                setBassBoostStrength(bass)
                setVirtualizerStrength(virt)
                setLoudnessGain(loud)
                setBandLevel(0, b1)
                setBandLevel(1, b2)
                setBandLevel(2, b3)
                setBandLevel(3, b4)
                setBandLevel(4, b5)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setEffectsEnabled(enabled: Boolean) {
        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
            virtualizer?.enabled = enabled
            loudnessEnhancer?.enabled = enabled
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setBassBoostStrength(strength: Int) {
        try {
            if (bassBoost?.strengthSupported == true) {
                bassBoost?.setStrength(strength.toShort())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setVirtualizerStrength(strength: Int) {
        try {
            if (virtualizer?.strengthSupported == true) {
                virtualizer?.setStrength(strength.toShort())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setLoudnessGain(gainMilliBel: Int) {
        try {
            loudnessEnhancer?.setTargetGain(gainMilliBel)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setBandLevel(band: Int, valueDb: Float) {
        try {
            val eq = equalizer ?: return
            if (band < eq.numberOfBands) {
                // Convert Db to millibel
                val milliBel = (valueDb * 100).toInt().toShort()
                eq.setBandLevel(band.toShort(), milliBel)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun releaseEffects() {
        try {
            equalizer?.release()
            equalizer = null
            bassBoost?.release()
            bassBoost = null
            virtualizer?.release()
            virtualizer = null
            loudnessEnhancer?.release()
            loudnessEnhancer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
