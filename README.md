#### Some background
<small>It all began as a game. The plan was to create a cipher that yields a completely different ciphertext
for every change in the input, with a result as random as a message digest.

The first step towards a cipher was a key derivation function. Hence the katakerm algorithm,
which has the basic properties of a hash function. Crafting it helped me comprehend one basic mechanism behind
every good hash function: cascading every difference of the input everywhere on the output.

That was my eureka! moment for the cipher: even the slightest change of a single bit
should yield a completely different result, i.e. it should be *cascaded* everywhere on the output.
It should just happen in a manner that does not hinder the retrieval of the original value.

After some weeks of trial and error, a couple of plans that were as doomed as the Coyote's, and 
several unexpected eureka! moments, my solution evolved to this:
a CBC structure with the message digest of both the plaintext and the password as initialization vector, and then just... well, just any cipher as usual.
Even a simple XOR operation might suffice.</small>


### An unbreakable (?) mode of encryption
#### and a sample implementation of it

The mode is surprisingly simple, though properly implementing it may require tremendous efforts:

![Encryption mode](cbc-encryption.png?raw=true)

The first step of the encryption is to get the message digest of the plaintext. 
It can then either be stored somewhere and be given by the user as input along with the key, 
or be chaffed (dispersed) through the ciphertext only to be winnowed (retrieved and removed) before the decryption,
or even just be pasted as is in a header. In a good cipher, knowing it without knowing the password should be of no value.
In this implementation, we chose the second approach.

Using it as an Initialization Vector (or simply as a complement of the key) makes sure that every change on the plaintext, 
however insignificant, is cascaded onto all the ciphertext in a chaotic manner.

And no matter how many plaintexts you encrypt with the same main key, they always appear as random as if they were just digests.
Many major forms of cryptanalysis are already rendered useless.

The second step is to use the digest and the key to encrypt the first block. In our implementation, we use blocks
of 1024 bytes. We then utilize the key and plaintext digest to generate a pad for the first block by using combinations 
of them to get some first message digests, which in turn are used to get some second message digests, until a pad of 1024 bytes
is generated. Unless there is a flaw in the underlying hash function, this pad is unique for every plaintext.

The next step is the encryption itself. In our implementation is a simple XOR operation. Even this should be sufficient.
But any more complex encryption algorithm could be used. The ciphertext is saved in the output file as is, 
end then permuted (shuffled) in a manner depending on the key itself, before being XORed to the second
block, along with the second digest of the key (K2).

It is not necessary that the keys used for each block are different; we use this approach here to keep the encryption algorithm
simple. 

![Decryption mode](cbc-decryption.png?raw=true)

