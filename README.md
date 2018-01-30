# NearbySample

This is an example to do a Nearby connection between devices. A device is the Advisor and the other is the Discover, and they exchange the object DataTransfered (with a text and a image in Base64) between them.

To test it, compile a device with 
```java
private static String MODE="ADVERTISING";
```
and other device with 
```java
private static String MODE ="DISCOVERY";
```

# References
[Google Nearby references](https://developers.google.com/android/reference/com/google/android/gms/nearby/connection/package-summary)
[Knowledge Transfer](http://androidkt.com/nearby-connections-api-2-0/)
[Nearby connection API](https://developers.google.com/nearby/connections/overview)
