#based on https://audioprograming.wordpress.com/2012/03/02/a-phase-vocoder-in-python/
import sys
from scipy import zeros, hanning, linspace, fft, ifft, angle, cos, sin, array
from numpy import round, arange
#from pylab import *
from scipy.io import wavfile

def speedx(sound_array, factor):
    """ Multiplies the sound's speed by some `factor` """
    indices = round( arange(0, len(sound_array), factor) )
    indices = indices[indices < len(sound_array)].astype(int)
    return sound_array[ indices.astype(int) ]

N = 2048
H = N/4

R = 300


# read input and get the timescale factor
(sr,signalin) = wavfile.read(sys.argv[2])
L = len(signalin)
tscale = float(sys.argv[1])
# signal blocks for processing and output
phi  = zeros(N)
out = zeros(N, dtype=complex)
sigout = zeros(L/tscale+N)
sigout2 = zeros((L/tscale+N)*tscale)

# max input amp, window
amp = max(signalin)
win = hanning(N)
p = 0
pp = 0

signalx = linspace(0,amp,R)

while p < L-(N+H):

    # take the spectra of two consecutive windows
    p1 = int(p)
    spec1 =  fft(win*signalin[p1:p1+N])
    spec2 =  fft(win*signalin[p1+H:p1+N+H])
    # take their phase difference and integrate
    phi += (angle(spec2) - angle(spec1))
    out.real, out.imag = cos(phi), sin(phi)
    # inverse FFT and overlap-add
    sigout[pp:pp+N] += win*ifft(abs(spec2)*out)
    pp += H
    p += H*tscale

i = 0
while (i < len(sigout)-R):
    #sigout[i:i+R] *= signalx
    i += R

sigout2 = speedx(sigout, float(1)/tscale)
#if tscale==0.5:
#    for j in range(len(sigout)):
#        if j%2==0:
#            sigout2[j/2] = sigout[j]

#  write file to output, scaling it to original amp

wavfile.write(sys.argv[3],sr,array(amp*sigout2/max(sigout2), dtype='int16'))

