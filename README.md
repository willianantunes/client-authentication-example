Simple Client Authentication
===========

You can quick test and send an encrypted handshake message with this Java project. Please be aware that you must create your self-signed certificate for example and deliver the issuer to your target. After that you can send a request as long as you have the target's CA in your trust store.

For the purpose of this lab, I do recommend to import the target's CA instead of disabling certificate validation.