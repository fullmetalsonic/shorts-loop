package com.fullmetalsonic.shortsloop.core;

import java.nio.charset.StandardCharsets;
import org.junit.Test;
import static org.junit.Assert.*;

public class UpdatePolicyTest {
    private static final String RELEASE = "https://github.com/fullmetalsonic/shorts-loop/releases/download/";
    private static final String ASSET_PATH = "v0.2.5/shorts-loop-v0.2.5.apk";
    private static final String HASH = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    private static final long DAY_MS = 86_400_000L;

    @Test public void repositoryAndSizeLimitsAreFixed() {
        assertEquals("fullmetalsonic/shorts-loop", UpdatePolicy.REPOSITORY);
        assertEquals("com.fullmetalsonic.shortsloop", UpdatePolicy.PACKAGE);
        assertEquals(41_943_040, UpdatePolicy.MAX_APK_BYTES);
        assertEquals(16_384, UpdatePolicy.MAX_MANIFEST_BYTES);
    }

    @Test public void versionAcceptsThreeNumericComponents() {
        for (String version : new String[]{"0.0.0", "0.2.5", "1.20.300", "100.0.2"}) {
            assertTrue(version, UpdatePolicy.validVersionName(version));
        }
    }

    @Test public void versionRejectsNonReleaseFormats() {
        for (String version : new String[]{"", "v0.2.5", "0.2", "0.2.5.1", "0.2.5-beta",
                "0.2.5+build", "-1.2.5", "0.+2.5", "0.2.x"}) {
            assertFalse(version, UpdatePolicy.validVersionName(version));
        }
        assertFalse(UpdatePolicy.validVersionName(null));
    }

    @Test public void versionRejectsLeadingZerosAndNonAsciiDigits() {
        for (String version : new String[]{"00.2.5", "0.02.5", "0.2.05", "０.２.５", "0.٢.5",
                " 0.2.5", "0.2.5 ", "0.2.5\n"}) {
            assertFalse(version, UpdatePolicy.validVersionName(version));
        }
    }

    @Test public void versionCodeRejectsSameVersionAndDowngrade() {
        assertTrue(UpdatePolicy.isNewer(13, 14));
        assertFalse(UpdatePolicy.isNewer(14, 14));
        assertFalse(UpdatePolicy.isNewer(14, 13));
    }

    @Test public void versionCodeRequiresPositiveAndBoundedCodes() {
        assertFalse(UpdatePolicy.isNewer(0, 14));
        assertFalse(UpdatePolicy.isNewer(-1, 14));
        assertFalse(UpdatePolicy.isNewer(1, 0));
        assertFalse(UpdatePolicy.isNewer(1, -1));
        assertTrue(UpdatePolicy.isNewer(Integer.MAX_VALUE - 1L, Integer.MAX_VALUE));
        assertFalse(UpdatePolicy.isNewer(1, Integer.MAX_VALUE + 1L));
        assertFalse(UpdatePolicy.isNewer(1, Long.MAX_VALUE));
        assertFalse(UpdatePolicy.isNewer(Long.MIN_VALUE, Long.MAX_VALUE));
    }

    @Test public void compatibilityHonorsCandidateMinimum() {
        assertTrue(UpdatePolicy.compatible(26, 26));
        assertTrue(UpdatePolicy.compatible(35, 26));
        assertTrue(UpdatePolicy.compatible(34, 34));
        assertFalse(UpdatePolicy.compatible(33, 34));
        assertFalse(UpdatePolicy.compatible(25, 26));
    }

    @Test public void compatibilityRejectsInvalidMinimumInsteadOfGuessing() {
        for (int minimum : new int[]{Integer.MIN_VALUE, -1, 0, 25}) {
            assertFalse(UpdatePolicy.compatible(35, minimum));
        }
        assertFalse(UpdatePolicy.compatible(-1, 26));
    }

    @Test public void shaMetadataAcceptsExactlySixtyFourHexCharacters() {
        assertTrue(UpdatePolicy.validSha256(HASH));
        assertTrue(UpdatePolicy.validSha256(HASH.toUpperCase(java.util.Locale.ROOT)));
        assertFalse(UpdatePolicy.validSha256(HASH.substring(1)));
        assertFalse(UpdatePolicy.validSha256(HASH + "0"));
        assertFalse(UpdatePolicy.validSha256("g" + HASH.substring(1)));
        assertFalse(UpdatePolicy.validSha256(HASH + "\n"));
        assertFalse(UpdatePolicy.validSha256(null));
    }

    @Test public void assetNameAllowsSimpleAsciiReleaseFiles() {
        assertTrue(UpdatePolicy.validAssetName("shorts-loop-v0.2.5.apk"));
        assertTrue(UpdatePolicy.validAssetName("release_14.APK"));
        assertTrue(UpdatePolicy.validAssetName("update.json"));
    }

    @Test public void assetNameRejectsTraversalAndSeparators() {
        for (String name : new String[]{"", ".", "..", "../app.apk", "folder/app.apk",
                "folder\\app.apk", "/app.apk", "C:app.apk", "app%2f.apk", "app?x.apk", "app#x.apk"}) {
            assertFalse(name, UpdatePolicy.validAssetName(name));
        }
        assertFalse(UpdatePolicy.validAssetName(null));
    }

    @Test public void assetNameRejectsUnicodeWhitespaceAndControls() {
        for (String name : new String[]{"쇼츠.apk", "app name.apk", "app.apk\n", "app\u0000.apk", "app\t.apk"}) {
            assertFalse(name, UpdatePolicy.validAssetName(name));
        }
    }

    @Test public void releaseUrlAcceptsOnlyCanonicalReleaseShape() {
        assertTrue(UpdatePolicy.trustedReleaseUrl(RELEASE + ASSET_PATH));
        assertTrue(UpdatePolicy.trustedReleaseUrl(RELEASE + "v0.2.5/update.json"));
        assertTrue(UpdatePolicy.trustedReleaseUrl("HTTPS://GITHUB.COM:443/fullmetalsonic/shorts-loop/releases/download/" + ASSET_PATH));
    }

    @Test public void releaseUrlRejectsOtherRepositoriesAndNonDownloadPaths() {
        for (String path : new String[]{"fullmetalsonic/other/releases/download/", "other/shorts-loop/releases/download/",
                "fullmetalsonic/shorts-loop/releases/latest/", "fullmetalsonic/shorts-loop/releases/tag/",
                "Fullmetalsonic/shorts-loop/releases/download/", "fullmetalsonic/shorts-loop/raw/"}) {
            assertFalse(path, UpdatePolicy.trustedReleaseUrl("https://github.com/" + path + ASSET_PATH));
        }
    }

    @Test public void releaseUrlRejectsMissingOrUnsafeTagsAndAssets() {
        for (String tail : new String[]{"", "v0.2.5", "v0.2.5/", "0.2.5/app.apk", "v0.02.5/app.apk",
                "v0.2.5-beta/app.apk", "v0.2.5/../app.apk", "v0.2.5/..", "v0.2.5/.",
                "v0.2.5/dir/app.apk", "v0.2.5/app.apk/", "/v0.2.5/app.apk"}) {
            assertFalse(tail, UpdatePolicy.trustedReleaseUrl(RELEASE + tail));
        }
    }

    @Test public void releaseUrlRejectsPercentEncodingAndBackslashes() {
        for (String tail : new String[]{"v0.2.5/app%2eapk", "v0.2.5/a%2fb.apk", "v0.2.5/%2e%2e",
                "v0.2.5/%252e%252e", "v0.2.5/a\\b.apk", "v0.2.5/app.apk%00"}) {
            assertFalse(tail, UpdatePolicy.trustedReleaseUrl(RELEASE + tail));
        }
        assertFalse(UpdatePolicy.trustedReleaseUrl(RELEASE.replace("shorts-loop", "%73horts-loop") + ASSET_PATH));
    }

    @Test public void releaseUrlRejectsNonHttpsAndRelativeAddresses() {
        assertFalse(UpdatePolicy.trustedReleaseUrl(RELEASE.replace("https:", "http:") + ASSET_PATH));
        assertFalse(UpdatePolicy.trustedReleaseUrl(RELEASE.replace("https:", "ftp:") + ASSET_PATH));
        assertFalse(UpdatePolicy.trustedReleaseUrl(RELEASE.replace("https:", "") + ASSET_PATH));
        assertFalse(UpdatePolicy.trustedReleaseUrl("/fullmetalsonic/shorts-loop/releases/download/" + ASSET_PATH));
    }

    @Test public void releaseUrlRejectsUserInfoPortsQueriesAndFragments() {
        for (String authority : new String[]{"user@github.com", "user:secret@github.com", "@github.com",
                "github.com:80", "github.com:444"}) {
            assertFalse(authority, UpdatePolicy.trustedReleaseUrl(RELEASE.replace("github.com", authority) + ASSET_PATH));
        }
        for (String suffix : new String[]{"?", "?download=1", "#", "#fragment"}) {
            assertFalse(suffix, UpdatePolicy.trustedReleaseUrl(RELEASE + ASSET_PATH + suffix));
        }
    }

    @Test public void releaseUrlRejectsSpoofedHosts() {
        for (String host : new String[]{"github.com.evil.example", "evilgithub.com", "github.com.",
                "github.com@evil.example", "github-com.example", "127.0.0.1", "[::1]"}) {
            assertFalse(host, UpdatePolicy.trustedReleaseUrl(RELEASE.replace("github.com", host) + ASSET_PATH));
        }
    }

    @Test public void invalidUrlsReturnFalseWithoutThrowing() {
        for (String url : new String[]{null, "", "not a URL", "https://", "https:github.com/file", "https://github.com/%zz",
                RELEASE + ASSET_PATH + "\n", " " + RELEASE + ASSET_PATH}) {
            assertFalse(UpdatePolicy.trustedReleaseUrl(url));
            assertFalse(UpdatePolicy.trustedRedirectUrl(url));
        }
    }

    @Test public void redirectAllowsGitHubReleaseOrSignedAssetCdn() {
        assertTrue(UpdatePolicy.trustedRedirectUrl(RELEASE + ASSET_PATH));
        assertTrue(UpdatePolicy.trustedRedirectUrl("https://release-assets.githubusercontent.com/github-production-release-asset/1/app?sig=abc&se=2026-08-28T00%3A00%3A00Z"));
        assertTrue(UpdatePolicy.trustedRedirectUrl("https://objects.githubusercontent.com/github-production-release-asset/1/app?X-Amz-Signature=abc"));
        assertTrue(UpdatePolicy.trustedRedirectUrl("HTTPS://RELEASE-ASSETS.GITHUBUSERCONTENT.COM:443/path?sig=abc"));
    }

    @Test public void redirectDoesNotAllowArbitraryGithubOrContentHosts() {
        for (String url : new String[]{"https://github.com/", "https://github.com/other/repo/releases/download/" + ASSET_PATH,
                RELEASE + ASSET_PATH + "?download=1", "https://raw.githubusercontent.com/repo/file",
                "https://avatars.githubusercontent.com/image", "https://release-assets.githubusercontent.com.evil.example/app",
                "https://evilrelease-assets.githubusercontent.com/app", "https://objects.githubusercontent.com./app"}) {
            assertFalse(url, UpdatePolicy.trustedRedirectUrl(url));
        }
    }

    @Test public void redirectRejectsCredentialsInsecurePortsAndFragments() {
        for (String url : new String[]{"http://release-assets.githubusercontent.com/app", "https://user@objects.githubusercontent.com/app",
                "https://objects.githubusercontent.com:80/app", "https://objects.githubusercontent.com:444/app",
                "https://objects.githubusercontent.com/app#", "https://objects.githubusercontent.com/app?sig=abc#frag",
                "//objects.githubusercontent.com/app", "https://objects.githubusercontent.com/app\r\nHost:evil.example"}) {
            assertFalse(url, UpdatePolicy.trustedRedirectUrl(url));
        }
    }

    @Test public void firstAttemptIsAllowed() {
        assertTrue(UpdatePolicy.shouldCheck(0, 0));
        assertTrue(UpdatePolicy.shouldCheck(1, 0));
        assertTrue(UpdatePolicy.shouldCheck(Long.MAX_VALUE, 0));
    }

    @Test public void checkingIntervalIncludesExactlyTwentyFourHours() {
        long last = 1_000;
        assertFalse(UpdatePolicy.shouldCheck(last, last));
        assertFalse(UpdatePolicy.shouldCheck(last + DAY_MS - 1, last));
        assertTrue(UpdatePolicy.shouldCheck(last + DAY_MS, last));
        assertTrue(UpdatePolicy.shouldCheck(last + DAY_MS + 1, last));
    }

    @Test public void backwardClockAllowsRecovery() {
        assertTrue(UpdatePolicy.shouldCheck(999, 1_000));
        assertTrue(UpdatePolicy.shouldCheck(0, 1_000));
        assertTrue(UpdatePolicy.shouldCheck(Long.MIN_VALUE, Long.MAX_VALUE));
    }

    @Test public void intervalCannotOverflowNearMaximumTimestamp() {
        assertFalse(UpdatePolicy.shouldCheck(Long.MAX_VALUE, Long.MAX_VALUE));
        assertFalse(UpdatePolicy.shouldCheck(Long.MAX_VALUE, Long.MAX_VALUE - DAY_MS + 1));
        assertTrue(UpdatePolicy.shouldCheck(Long.MAX_VALUE, Long.MAX_VALUE - DAY_MS));
    }

    @Test public void extremeTimestampDifferenceCannotWrapToNegative() {
        assertTrue(UpdatePolicy.shouldCheck(Long.MAX_VALUE, Long.MIN_VALUE));
        assertTrue(UpdatePolicy.shouldCheck(1, Long.MIN_VALUE));
        assertFalse(UpdatePolicy.shouldCheck(Long.MIN_VALUE + DAY_MS - 1, Long.MIN_VALUE));
        assertTrue(UpdatePolicy.shouldCheck(Long.MIN_VALUE + DAY_MS, Long.MIN_VALUE));
    }

    @Test public void sha256MatchesEmptyInputKnownVector() {
        assertEquals(HASH, UpdatePolicy.sha256(new byte[0]));
    }

    @Test public void sha256MatchesAbcKnownVector() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                UpdatePolicy.sha256("abc".getBytes(StandardCharsets.UTF_8)));
    }

    @Test public void sha256MatchesHelloWorldKnownVectorInLowercase() {
        String hash = UpdatePolicy.sha256("hello world".getBytes(StandardCharsets.UTF_8));
        assertEquals("b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9", hash);
        assertTrue(UpdatePolicy.validSha256(hash));
    }

    @Test public void sha256DoesNotModifyInputAndDifferentContentDiffers() {
        byte[] input = new byte[]{0, 1, 2, (byte) 255};
        byte[] original = input.clone();
        String hash = UpdatePolicy.sha256(input);
        assertArrayEquals(original, input);
        assertEquals(hash, UpdatePolicy.sha256(original));
        assertNotEquals(hash, UpdatePolicy.sha256(new byte[]{0, 1, 2, (byte) 254}));
    }

    @Test(expected = NullPointerException.class) public void sha256RejectsNullInput() {
        UpdatePolicy.sha256(null);
    }
}
