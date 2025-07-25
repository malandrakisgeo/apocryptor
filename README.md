#### Some background
<sup>It all began as a game. The plan was to create a cipher that yields a completely different ciphertext
for every change in the input, with a result as random as possible from the attacker's perspective.

<sup>The first step towards a cipher was a key derivation function. Hence the [katakerm algorithm](https://github.com/malandrakisgeo/katakerm)
which has the basic properties of a hash function. Crafting it helped me comprehend the basic mechanism behind hashing: cascading every difference of the input everywhere on the output.

<sup>That was my eureka! moment for the cipher: even the slightest change of a single bit should yield a completely different result, i.e. it should be *cascaded* everywhere on the output.
But unlike in hash functions, it should be done in a way that does not hinder the retrieval of the original plaintext.

<sup>After some weeks of trial and error, a couple of plans that were as doomed as the Coyote's, and some other eureka! moments, my solution evolved to this:
a CBC structure with the message digest of both the plaintext and the password as initialization vector, and then just... well, just any byte-by-byte modification of the input.
Even a simple XOR operation might suffice.


### An unbreakable (?) block cipher mode of operation
#### and a sample implementation of it

The mode is surprisingly simple, though flawlessly implementing it can be very demanding:

![Encryption mode](cbc-encryption.png?raw=true)

The first step of the encryption is to get the message digest of the plaintext. 
It can then either be stored somewhere and be given by the user as input along with the key, 
or be chaffed (dispersed) through the ciphertext only to be winnowed (retrieved and removed) before the decryption,
or even just be pasted as is in a header. No matter the approach, in a good cipher, knowing the original digest without knowing the password should be of no value.

Using the file's digest as an Initialization Vector (or simply as a complement of the key) makes sure that every change on the plaintext, 
however insignificant, is cascaded onto all the ciphertext in a chaotic manner.  And no matter how many plaintexts you encrypt with the same main key, 
they always appear as random as if they were just digests. Many major forms of cryptanalysis are already rendered useless.

The second step is to use the digest and the key to encrypt the first block. The first block could
use separate cipher or pad-generator, using the key and the digest to generate a unique pad of equal length to the block (as APOCRYPTOR does). 
Unless there is a flaw in the underlying hash function, this pad is in practice unique for every plaintext. If
the block cipher is sufficiently complex, it should suffice with no need for a pad generator for the first block.

The third step, after encrypting the first block and saving it, is getting a key-dependent permutation (shuffling) of the result,
and use it as input to the block cipher -either as a simple XOR operation, or with any more complex algorithm.

One could, of course, save the shuffled ciphertext in the encrypted file,  but this could weaken the algorithm:
it would allow an attacker to know exactly which input was used for the next block. 
Using a key-based permutation of the ciphertext rather than the ciphertext itself, is essential to prevent attacks.

The keys used for each block may differ (e.g. n-th digest of the original key for the n-th block),
but it is not necessary.  The key(s) used for shuffling could be derived from the combination of the digest and the password as well, hence making the shuffling
different for every input.

![Decryption mode](cbc-decryption.png?raw=true)

On decryption, the first step is retrieving the message digest of the original plaintext, if it was chaffed.
Or just getting it from the header, or any other method one used to store it.

We then regenerate the pad of the first block, and decrypt it. The ciphertext itself is then
shuffled according to K1, and used as input for the decryption of the second block. The second block would be shuffled
depending on K2 and used for the decryption of the third block. The And so on.

A proper implementation of this encryption/decryption mode should yield completely different results,
and random from the attackers perspective, and be unbreakable.

### APOCRYPTOR: a proof of concept

APOCRYPTOR is a sample implementation of this encryption mode, used just as a proof of concept.

In this implementation, the message digest or the original plaintext is chaffed (dispersed) through
the ciphertext, and winnoed (retrieved and removed from the file) right before the decryption.

Blocks  of 1024 bytes are used. 

We utilize the key and plaintext digest to generate a pad for the first block by using combinations
of them to get some first message digests, which in turn are used to get some second message digests, and so on, until a pad of 1024 bytes
is generated. 

The permutation step is a simple shuffling of the 32byte subblocks of the n-th block depending on the n-th digest of the original key. A more complex
permutation algorithm is under construction.

APOCRYPTOR as of 7/2025 is crudely written, and unsuitable for production use.

No measures are taken for wiping used RAM or preventing the OS from storing temporary data, e.g. the keys in plaintext.
Since the JVM cannot really prevent the OS from doing some things, APOCRYPTOR will remain a sample
implementation as a proof of concept.

The purpose of APOCRYPTOR is to test and verify that even a basic implementation of this "eureka!" cipher mode,
produces entirely different and apparently random ciphertexts for even the slightest change of the input or password used.

You can modify the "ENCRYPT_ME" file under /resources and/or change the password in APOCRYPTOR.java, and see the results for yourselves, in both the
console, and under your /target/classes.

