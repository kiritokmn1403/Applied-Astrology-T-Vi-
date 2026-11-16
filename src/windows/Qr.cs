// QR Code generator – bản C# rút gọn (chế độ byte UTF-8) chuyển từ Project Nayuki (MIT), https://github.com/nayuki/QR-Code-generator
using System;
using System.Collections.Generic;
using System.Text;

namespace AppliedAstrology.Core
{
    public sealed class QrCode
    {
        public enum Ecc { Low = 0, Medium = 1, Quartile = 2, High = 3 }
        static readonly int[] FORMAT_BITS = { 1, 0, 3, 2 };
        public readonly int Version, Size, Mask; readonly Ecc ecl; bool[,] modules, isFunction;
        static readonly sbyte[,] ECC_CODEWORDS_PER_BLOCK = {
            {-1,  7, 10, 15, 20, 26, 18, 20, 24, 30, 18, 20, 24, 26, 30, 22, 24, 28, 30, 28, 28, 28, 28, 30, 30, 26, 28, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30},
            {-1, 10, 16, 26, 18, 24, 16, 18, 22, 22, 26, 30, 22, 22, 24, 24, 28, 28, 26, 26, 26, 26, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28, 28},
            {-1, 13, 22, 18, 26, 18, 24, 18, 22, 20, 24, 28, 26, 24, 20, 30, 24, 28, 28, 26, 30, 28, 30, 30, 30, 30, 28, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30},
            {-1, 17, 28, 22, 16, 22, 28, 26, 26, 24, 28, 24, 28, 22, 24, 24, 30, 28, 28, 26, 28, 30, 24, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30}};
        static readonly sbyte[,] NUM_ERROR_CORRECTION_BLOCKS = {
            {-1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 4,  4,  4,  4,  4,  6,  6,  6,  6,  7,  8,  8,  9,  9, 10, 12, 12, 12, 13, 14, 15, 16, 17, 18, 19, 19, 20, 21, 22, 24, 25},
            {-1, 1, 1, 1, 2, 2, 4, 4, 4, 5, 5,  5,  8,  9,  9, 10, 10, 11, 13, 14, 16, 17, 17, 18, 20, 21, 23, 25, 26, 28, 29, 31, 33, 35, 37, 38, 40, 43, 45, 47, 49},
            {-1, 1, 1, 2, 2, 4, 4, 6, 6, 8, 8,  8, 10, 12, 16, 12, 17, 16, 18, 21, 20, 23, 23, 25, 27, 29, 34, 34, 35, 38, 40, 43, 45, 48, 51, 53, 56, 59, 62, 65, 68},
            {-1, 1, 1, 2, 4, 4, 4, 5, 6, 8, 8, 11, 11, 16, 16, 18, 16, 19, 21, 25, 25, 25, 34, 30, 32, 35, 37, 40, 42, 45, 48, 51, 54, 57, 60, 63, 66, 70, 74, 77, 81}};

        public static QrCode EncodeText(string text, Ecc ecl)
        {
            byte[] data = Encoding.UTF8.GetBytes(text); int version, used = 0;
            for (version = 1; ; version++)
            {
                int ccBits = version <= 9 ? 8 : 16; used = 4 + ccBits + data.Length * 8;
                if (used <= NumDataCodewords(version, ecl) * 8) break;
                if (version >= 40) throw new ArgumentException("Data too long");
            }
            foreach (Ecc e in new[] { Ecc.Medium, Ecc.Quartile, Ecc.High }) if (e > ecl && used <= NumDataCodewords(version, e) * 8) ecl = e;
            var bits = new List<bool>();
            Action<int, int> app = (val, len) => { for (int i = len - 1; i >= 0; i--) bits.Add(((val >> i) & 1) != 0); };
            app(4, 4); app(data.Length, version <= 9 ? 8 : 16); foreach (byte b in data) app(b, 8);
            int cap = NumDataCodewords(version, ecl) * 8;
            app(0, Math.Min(4, cap - bits.Count)); app(0, (8 - bits.Count % 8) % 8);
            for (int pad = 0xEC; bits.Count < cap; pad ^= 0xEC ^ 0x11) app(pad, 8);
            var dc = new byte[bits.Count / 8]; for (int i = 0; i < bits.Count; i++) if (bits[i]) dc[i >> 3] |= (byte)(1 << (7 - (i & 7)));
            return new QrCode(version, ecl, dc);
        }
        QrCode(int ver, Ecc e, byte[] dataCodewords)
        {
            Version = ver; Size = ver * 4 + 17; ecl = e; modules = new bool[Size, Size]; isFunction = new bool[Size, Size];
            DrawFunctionPatterns(); DrawCodewords(AddEccAndInterleave(dataCodewords));
            int best = 0, minPen = int.MaxValue;
            for (int i = 0; i < 8; i++) { ApplyMask(i); DrawFormatBits(i); int p = Penalty(); if (p < minPen) { minPen = p; best = i; } ApplyMask(i); }
            Mask = best; ApplyMask(best); DrawFormatBits(best); isFunction = null;
        }
        public bool Get(int x, int y) { return x >= 0 && x < Size && y >= 0 && y < Size && modules[y, x]; }
        void SetF(int x, int y, bool dark) { modules[y, x] = dark; isFunction[y, x] = true; }
        void DrawFunctionPatterns()
        {
            for (int i = 0; i < Size; i++) { SetF(6, i, i % 2 == 0); SetF(i, 6, i % 2 == 0); }
            Finder(3, 3); Finder(Size - 4, 3); Finder(3, Size - 4);
            var al = AlignPos(); int n = al.Length;
            for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) if (!(i == 0 && j == 0 || i == 0 && j == n - 1 || i == n - 1 && j == 0)) for (int dy = -2; dy <= 2; dy++) for (int dx = -2; dx <= 2; dx++) SetF(al[i] + dx, al[j] + dy, Math.Max(Math.Abs(dx), Math.Abs(dy)) != 1);
            DrawFormatBits(0);
            if (Version >= 7)
            {
                int rem = Version; for (int i = 0; i < 12; i++) rem = (rem << 1) ^ ((rem >> 11) * 0x1F25);
                int bits = Version << 12 | rem;
                for (int i = 0; i < 18; i++) { bool bit = ((bits >> i) & 1) != 0; int a = Size - 11 + i % 3, b = i / 3; SetF(a, b, bit); SetF(b, a, bit); }
            }
        }
        void Finder(int x, int y) { for (int dy = -4; dy <= 4; dy++) for (int dx = -4; dx <= 4; dx++) { int d = Math.Max(Math.Abs(dx), Math.Abs(dy)), xx = x + dx, yy = y + dy; if (xx >= 0 && xx < Size && yy >= 0 && yy < Size) SetF(xx, yy, d != 2 && d != 4); } }
        void DrawFormatBits(int msk)
        {
            int data = FORMAT_BITS[(int)ecl] << 3 | msk, rem = data;
            for (int i = 0; i < 10; i++) rem = (rem << 1) ^ ((rem >> 9) * 0x537);
            int bits = (data << 10 | rem) ^ 0x5412;
            Func<int, bool> g = i => ((bits >> i) & 1) != 0;
            for (int i = 0; i <= 5; i++) SetF(8, i, g(i));
            SetF(8, 7, g(6)); SetF(8, 8, g(7)); SetF(7, 8, g(8));
            for (int i = 9; i < 15; i++) SetF(14 - i, 8, g(i));
            for (int i = 0; i < 8; i++) SetF(Size - 1 - i, 8, g(i));
            for (int i = 8; i < 15; i++) SetF(8, Size - 15 + i, g(i));
            SetF(8, Size - 8, true);
        }
        int[] AlignPos()
        {
            if (Version == 1) return new int[0];
            int num = Version / 7 + 2, step = (Version * 8 + num * 3 + 5) / (num * 4 - 4) * 2; var r = new int[num]; r[0] = 6;
            for (int i = r.Length - 1, pos = Size - 7; i >= 1; i--, pos -= step) r[i] = pos;
            return r;
        }
        static int RawModules(int ver)
        {
            int size = ver * 4 + 17, r = size * size - 8 * 8 * 3 - (15 * 2 + 1) - (size - 16) * 2;
            if (ver >= 2) { int n = ver / 7 + 2; r -= (n - 1) * (n - 1) * 25; r -= (n - 2) * 2 * 20; if (ver >= 7) r -= 36; }
            return r;
        }
        static int NumDataCodewords(int ver, Ecc e) { return RawModules(ver) / 8 - ECC_CODEWORDS_PER_BLOCK[(int)e, ver] * NUM_ERROR_CORRECTION_BLOCKS[(int)e, ver]; }
        byte[] AddEccAndInterleave(byte[] data)
        {
            int nb = NUM_ERROR_CORRECTION_BLOCKS[(int)ecl, Version], el = ECC_CODEWORDS_PER_BLOCK[(int)ecl, Version], raw = RawModules(Version) / 8;
            int nShort = nb - raw % nb, shortLen = raw / nb; var blocks = new byte[nb][]; var div = RsDivisor(el);
            for (int i = 0, k = 0; i < nb; i++)
            {
                int len = shortLen - el + (i < nShort ? 0 : 1); var dat = new byte[len]; Array.Copy(data, k, dat, 0, len); k += len;
                var block = new byte[shortLen + 1]; Array.Copy(dat, block, len); var ecc = RsRemainder(dat, div); Array.Copy(ecc, 0, block, block.Length - el, ecc.Length); blocks[i] = block;
            }
            var res = new byte[raw];
            for (int i = 0, k = 0; i < blocks[0].Length; i++) for (int j = 0; j < blocks.Length; j++) if (i != shortLen - el || j >= nShort) res[k++] = blocks[j][i];
            return res;
        }
        void DrawCodewords(byte[] data)
        {
            int i = 0;
            for (int right = Size - 1; right >= 1; right -= 2)
            {
                if (right == 6) right = 5;
                for (int vert = 0; vert < Size; vert++) for (int j = 0; j < 2; j++)
                    {
                        int x = right - j; bool up = ((right + 1) & 2) == 0; int y = up ? Size - 1 - vert : vert;
                        if (!isFunction[y, x] && i < data.Length * 8) { modules[y, x] = ((data[i >> 3] >> (7 - (i & 7))) & 1) != 0; i++; }
                    }
            }
        }
        void ApplyMask(int m)
        {
            for (int y = 0; y < Size; y++) for (int x = 0; x < Size; x++)
                {
                    bool inv;
                    switch (m) { case 0: inv = (x + y) % 2 == 0; break; case 1: inv = y % 2 == 0; break; case 2: inv = x % 3 == 0; break; case 3: inv = (x + y) % 3 == 0; break; case 4: inv = (x / 3 + y / 2) % 2 == 0; break; case 5: inv = x * y % 2 + x * y % 3 == 0; break; case 6: inv = (x * y % 2 + x * y % 3) % 2 == 0; break; default: inv = ((x + y) % 2 + x * y % 3) % 2 == 0; break; }
                    if (inv && !isFunction[y, x]) modules[y, x] = !modules[y, x];
                }
        }
        int Penalty()
        {
            int result = 0;
            for (int pass = 0; pass < 2; pass++)
                for (int a = 0; a < Size; a++)
                {
                    bool runColor = false; int run = 0; var hist = new int[7];
                    for (int b = 0; b < Size; b++)
                    {
                        bool c = pass == 0 ? modules[a, b] : modules[b, a];
                        if (c == runColor) { run++; if (run == 5) result += 3; else if (run > 5) result++; }
                        else { AddHist(run, hist); if (!runColor) result += CountPat(hist) * 40; runColor = c; run = 1; }
                    }
                    if (runColor) { AddHist(run, hist); run = 0; }
                    run += Size; AddHist(run, hist); result += CountPat(hist) * 40;
                }
            for (int y = 0; y < Size - 1; y++) for (int x = 0; x < Size - 1; x++) { bool c = modules[y, x]; if (c == modules[y, x + 1] && c == modules[y + 1, x] && c == modules[y + 1, x + 1]) result += 3; }
            int dark = 0; foreach (bool c in modules) if (c) dark++;
            int total = Size * Size, k = (Math.Abs(dark * 20 - total * 10) + total - 1) / total - 1;
            return result + k * 10;
        }
        void AddHist(int run, int[] h) { if (h[0] == 0) run += Size; Array.Copy(h, 0, h, 1, h.Length - 1); h[0] = run; }
        int CountPat(int[] h) { int n = h[1]; bool core = n > 0 && h[2] == n && h[3] == n * 3 && h[4] == n && h[5] == n; return (core && h[0] >= n * 4 && h[6] >= n ? 1 : 0) + (core && h[6] >= n * 4 && h[0] >= n ? 1 : 0); }
        static byte[] RsDivisor(int degree)
        {
            var r = new byte[degree]; r[degree - 1] = 1; int root = 1;
            for (int i = 0; i < degree; i++) { for (int j = 0; j < r.Length; j++) { r[j] = (byte)RsMul(r[j], root); if (j + 1 < r.Length) r[j] ^= r[j + 1]; } root = RsMul(root, 2); }
            return r;
        }
        static byte[] RsRemainder(byte[] data, byte[] div)
        {
            var r = new byte[div.Length];
            foreach (byte b in data) { int f = (b ^ r[0]) & 0xFF; Array.Copy(r, 1, r, 0, r.Length - 1); r[r.Length - 1] = 0; for (int i = 0; i < r.Length; i++) r[i] ^= (byte)RsMul(div[i], f); }
            return r;
        }
        static int RsMul(int x, int y) { int z = 0; for (int i = 7; i >= 0; i--) { z = (z << 1) ^ ((z >> 7) * 0x11D); z ^= ((y >> i) & 1) * x; } return z; }
    }
}
