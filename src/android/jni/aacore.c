/*
 * Applied Astrology v0.0.2 - libaacore.so
 * Tìm kiếm toàn văn thư viện Bửu Đình ngay trong bộ nhớ native (NDK), nhanh hơn JS trên máy yếu.
 * Đọc trực tiếp assets/web/data/lib_NN.js qua AAssetManager, giải mã chuỗi JSON, bỏ dấu tiếng Việt
 * (1 ký tự UTF-16 -> 1 byte, để vị trí khớp với chỉ số chuỗi bên JavaScript).
 */
#include <jni.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include "fold_table.h"

#define NCH 13
typedef struct { char *title; char *body; int tlen, blen; } Article;
typedef struct { Article *a; int n; int loaded; } Chapter;
static Chapter g_ch[NCH + 1];

static char fold_cp(unsigned int cp) {
    if (cp < 128) return (cp >= 'A' && cp <= 'Z') ? (char)(cp + 32) : (char)cp;
    int lo = 0, hi = FOLD_N - 1;
    while (lo <= hi) {
        int mid = (lo + hi) / 2;
        if (FOLD_CP[mid] == cp) return FOLD_CH[mid];
        if (FOLD_CP[mid] < cp) lo = mid + 1; else hi = mid - 1;
    }
    return '?';
}

static int hexv(char c) {
    if (c >= '0' && c <= '9') return c - '0';
    if (c >= 'a' && c <= 'f') return c - 'a' + 10;
    if (c >= 'A' && c <= 'F') return c - 'A' + 10;
    return 0;
}

/* Giải mã chuỗi JSON bắt đầu sau dấu " tại *pp; trả về chuỗi đã bỏ dấu (malloc) */
static char *read_json_string(const char **pp, const char *end, int *outLen) {
    const unsigned char *p = (const unsigned char *)*pp;
    int cap = 256, n = 0;
    char *out = (char *)malloc(cap);
    while ((const char *)p < end && *p != '"') {
        unsigned int cp; int units = 1;
        if (*p == '\\') {
            p++;
            switch (*p) {
                case 'n': cp = ' '; p++; break;  /* xuống dòng -> khoảng trắng (1 ký tự như JS '\n') */
                case 'r': case 't': case 'b': case 'f': cp = ' '; p++; break;
                case 'u': {
                    cp = (hexv(p[1]) << 12) | (hexv(p[2]) << 8) | (hexv(p[3]) << 4) | hexv(p[4]);
                    p += 5; break;
                }
                default: cp = *p; p++; break;
            }
        } else if (*p < 0x80) { cp = *p++; }
        else if ((*p & 0xE0) == 0xC0) { cp = ((p[0] & 0x1F) << 6) | (p[1] & 0x3F); p += 2; }
        else if ((*p & 0xF0) == 0xE0) { cp = ((p[0] & 0x0F) << 12) | ((p[1] & 0x3F) << 6) | (p[2] & 0x3F); p += 3; }
        else { cp = '?'; units = 2; p += 4; }  /* ký tự ngoài BMP = 2 đơn vị UTF-16 */
        while (n + units + 1 >= cap) { cap *= 2; out = (char *)realloc(out, cap); }
        if (units == 2) { out[n++] = '?'; out[n++] = '?'; }
        else out[n++] = fold_cp(cp);
    }
    out[n] = 0;
    if ((const char *)p < end) p++; /* bỏ dấu " đóng */
    *pp = (const char *)p;
    *outLen = n;
    return out;
}

static void load_chapter(AAssetManager *am, int no) {
    Chapter *c = &g_ch[no];
    if (c->loaded) return;
    c->loaded = 1;
    char path[64];
    snprintf(path, sizeof path, "web/data/lib_%02d.js", no);
    AAsset *as = AAssetManager_open(am, path, AASSET_MODE_BUFFER);
    if (!as) return;
    const char *buf = (const char *)AAsset_getBuffer(as);
    off_t len = AAsset_getLength(as);
    const char *p = buf, *end = buf + len;
    /* bỏ qua tham số đầu tiên ("id") rồi tới mảng bài viết */
    p = memchr(p, ',', end - p);
    if (!p) { AAsset_close(as); return; }
    int cap = 64;
    c->a = (Article *)calloc(cap, sizeof(Article));
    int expectTitle = 1;
    while (p < end) {
        if (*p == '"') {
            p++;
            int l; char *s = read_json_string(&p, end, &l);
            if (expectTitle) {
                if (c->n >= cap) { cap *= 2; c->a = (Article *)realloc(c->a, cap * sizeof(Article)); }
                c->a[c->n].title = s; c->a[c->n].tlen = l; c->a[c->n].body = NULL;
            } else {
                c->a[c->n].body = s; c->a[c->n].blen = l; c->n++;
            }
            expectTitle = !expectTitle;
        } else p++;
    }
    AAsset_close(as);
}

static int count_occ(const char *hay, const char *needle, int nlen, int *first) {
    int cnt = 0; const char *p = hay; *first = -1;
    while ((p = strstr(p, needle)) != NULL && cnt < 20) {
        if (*first < 0) *first = (int)(p - hay);
        cnt++; p += nlen;
    }
    return cnt;
}

typedef struct { int c, i, o, s; } Hit;
static int cmp_hit(const void *a, const void *b) { return ((const Hit *)b)->s - ((const Hit *)a)->s; }

JNIEXPORT jstring JNICALL
Java_com_appliedastrology_tuvi_NativeCore_nativeAbi(JNIEnv *env, jclass cls) {
#if defined(__aarch64__)
    const char *abi = "arm64-v8a";
#elif defined(__arm__)
    const char *abi = "armeabi-v7a";
#elif defined(__x86_64__)
    const char *abi = "x86_64";
#elif defined(__i386__)
    const char *abi = "x86";
#else
    const char *abi = "unknown";
#endif
    return (*env)->NewStringUTF(env, abi);
}

JNIEXPORT jstring JNICALL
Java_com_appliedastrology_tuvi_NativeCore_nativeSearch(JNIEnv *env, jclass cls, jobject jam, jstring jq, jint max) {
    AAssetManager *am = AAssetManager_fromJava(env, jam);
    const char *q8 = (*env)->GetStringUTFChars(env, jq, NULL);
    /* bỏ dấu truy vấn bằng cùng bảng */
    const char *qp = q8; int ql;
    char *quoted = (char *)malloc(strlen(q8) + 2);
    /* tái dùng bộ giải mã: bọc chuỗi như JSON (không có ký tự đặc biệt vì JS đã fold) */
    strcpy(quoted, q8); strcat(quoted, "\"");
    const char *qq = quoted;
    char *q = read_json_string(&qq, quoted + strlen(quoted), &ql);
    free(quoted);
    (*env)->ReleaseStringUTFChars(env, jq, q8);
    (void)qp;
    /* tách từ cho chế độ "đủ các từ" */
    char words[8][64]; int nw = 0;
    { char tmp[512]; strncpy(tmp, q, sizeof tmp - 1); tmp[sizeof tmp - 1] = 0;
      char *tok = strtok(tmp, " ");
      while (tok && nw < 8) { strncpy(words[nw], tok, 63); words[nw][63] = 0; nw++; tok = strtok(NULL, " "); } }

    int hcap = 256, hn = 0;
    Hit *hits = (Hit *)malloc(hcap * sizeof(Hit));
    for (int c = 1; c <= NCH; c++) {
        load_chapter(am, c);
        Chapter *ch = &g_ch[c];
        for (int i = 0; i < ch->n; i++) {
            Article *a = &ch->a[i];
            if (!a->body) continue;
            int first = -1, score = 0, f2;
            if (strstr(a->title, q)) score += 50;
            int cnt = count_occ(a->body, q, ql, &first);
            if (cnt > 0) score += 10 + cnt;
            else if (nw > 1) {
                int all = 1;
                for (int w = 0; w < nw; w++) if (!strstr(a->body, words[w]) && !strstr(a->title, words[w])) { all = 0; break; }
                if (all) { score += 3; count_occ(a->body, words[0], (int)strlen(words[0]), &f2); first = f2; }
            }
            if (score > 0) {
                if (hn >= hcap) { hcap *= 2; hits = (Hit *)realloc(hits, hcap * sizeof(Hit)); }
                hits[hn].c = c; hits[hn].i = i; hits[hn].o = first < 0 ? 0 : first; hits[hn].s = score; hn++;
            }
        }
    }
    free(q);
    qsort(hits, hn, sizeof(Hit), cmp_hit);
    if (hn > max) hn = max;
    char *out = (char *)malloc(hn * 48 + 4);
    int o = 0;
    out[o++] = '[';
    for (int k = 0; k < hn; k++) o += sprintf(out + o, "%s{\"c\":%d,\"i\":%d,\"o\":%d,\"s\":%d}", k ? "," : "", hits[k].c, hits[k].i, hits[k].o, hits[k].s);
    out[o++] = ']'; out[o] = 0;
    free(hits);
    jstring js = (*env)->NewStringUTF(env, out);
    free(out);
    return js;
}
