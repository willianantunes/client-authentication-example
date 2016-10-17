##################################################
#### Links (JKS format)
https://docs.oracle.com/cd/E19798-01/821-1841/gjrgy/index.html
http://docs.oracle.com/javase/6/docs/technotes/tools/solaris/keytool.html
http://stackoverflow.com/questions/1666052/java-https-client-certificate-authentication

# Create keystore and self-signed certificate
keytool -genkey 
-dname "CN=cn-test, OU=IT, O=EDG, L=S Paulo, ST=SP, C=BR" 
-alias self-signed-ag 
-keyalg RSA 
-keypass qwerty321 
-storepass qwerty321 
-keystore keystore.jks 
-validity 1825

# The code above in an inline aspect
keytool -genkey -dname "CN=cn-test, OU=IT, O=EDG, L=S Paulo, ST=SP, C=BR" -alias self-signed-ag -keyalg RSA -keypass qwerty321 -storepass qwerty321 -keystore keystore.jks -validity 1825

# Create CSR (only needed if you don't use the self-signed certificate above)
keytool -certreq -file myCsrFile.csr -keystore keystore.jks -storepass qwerty321 -keypass qwerty321 -alias self-signed-ag

# Import signed certificate (only if you export a CSR and signed it from a foreign CA)
keytool -importcert -alias self-signed-ag -file mySignedCsr.cer -keystore keystore.jks -storepass qwerty321

# Export the generated server certificate in keystore.jks into the file server.cer
keytool -export -alias self-signed-ag -storepass qwerty321 -file server.cer -keystore keystore.jks

# Create a PKCS12 (.pfx / .p12) from a JKS
keytool -importkeystore 
-srckeystore keystore.jks -srcstorepass qwerty321
-destkeystore keystore.p12 -deststorepass qwerty321
-srcstoretype JKS -deststoretype PKCS12  
-srcalias self-signed-ag -srckeypass qwerty321
-destalias self-signed-ag

# The code above in an inline aspect
keytool -importkeystore -srckeystore keystore.jks -srcstorepass qwerty321 -destkeystore keystore.p12 -srcstoretype JKS -deststoretype PKCS12 -deststorepass qwerty321 -srcalias self-signed-ag -srckeypass qwerty321 -destalias self-signed-ag

##################################################
#### Links (PKCS12 format)
https://docs.oracle.com/cd/E19509-01/820-3503/ggfhb/index.html
https://www.tbs-certificates.co.uk/FAQ/en/627.html

# Generate PKCS12 keystore
cat mykey.pem.txt mycertificate.pem.txt>mykeycertificate.pem.txt