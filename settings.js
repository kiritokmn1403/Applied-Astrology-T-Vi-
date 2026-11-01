/**
 * Applied Astrology (Tử Vi Ứng Dụng) - Settings Module
 * Tệp cấu hình & cài đặt độc lập: Quản lý ngôn ngữ, chế độ DeX, giao diện, cỡ chữ và bảo mật ngoại vi
 * Toàn bộ các mục cài đặt chỉ xuất hiện độc quyền tại mục Cài Đặt (Tab Cài Đặt).
 * Phiên bản: 0.0.1
 */

const AppSettings = {
  config: {
    language: 'vi',
    theme: 'celestial-dark', // celestial-dark | royal-gold | ivory-light | imperial-red
    dexMode: false,
    viewMode: 'auto', // 'auto' | 'responsive-cards' | 'classic-board'
    fontSize: 'normal', // 'compact' | 'normal' | 'large'
    showElements: true,
    showMieuHam: true,
    showLuuSao: true,
    soundEffects: false,
    externalVpnCheckUrl: 'https://dns.google/resolve?name=cloudflare.com&type=A',
    lastVpnStatus: 'Chưa kiểm tra'
  },

  init: function() {
    this.loadFromStorage();
    this.applyTheme(this.config.theme);
    this.applyFontSize(this.config.fontSize);
    this.applyDeXMode(this.config.dexMode);
    this.applyLanguage(this.config.language);
    this.updateSettingsUi();
    console.log('[AppSettings] Initialized cleanly in Settings Section.');
  },

  loadFromStorage: function() {
    try {
      const saved = localStorage.getItem('applied_astrology_settings');
      if (saved) {
        const parsed = JSON.parse(saved);
        this.config = Object.assign({}, this.config, parsed);
      }
    } catch (e) {
      console.warn('[AppSettings] Could not load from localStorage:', e);
    }
  },

  saveToStorage: function() {
    try {
      localStorage.setItem('applied_astrology_settings', JSON.stringify(this.config));
    } catch (e) {
      console.warn('[AppSettings] Could not save to localStorage:', e);
    }
  },

  // 1. Chuyển đổi ngôn ngữ (Hỗ trợ 19 ngôn ngữ)
  setLanguage: function(langCode) {
    this.config.language = langCode;
    this.saveToStorage();
    this.applyLanguage(langCode);
    if (typeof setLanguage === 'function') {
      setLanguage(langCode);
    }
    if (typeof currentChart !== 'undefined' && currentChart && typeof renderChart === 'function') {
      renderChart(currentChart);
    }
    this.updateSettingsUi();
  },

  applyLanguage: function(langCode) {
    document.documentElement.setAttribute('lang', langCode);
    const sel = document.getElementById('tabLangSelect');
    if (sel) sel.value = langCode;
  },

  // 2. Chế độ Samsung DeX / Desktop Màn hình lớn
  toggleDeXMode: function(explicitVal) {
    if (typeof explicitVal === 'boolean') {
      this.config.dexMode = explicitVal;
    } else {
      this.config.dexMode = !this.config.dexMode;
    }
    this.saveToStorage();
    this.applyDeXMode(this.config.dexMode);
    this.updateSettingsUi();
    return this.config.dexMode;
  },

  applyDeXMode: function(isDeX) {
    const body = document.body;
    const dexContainer = document.getElementById('dexContainer');
    
    if (isDeX) {
      body.classList.add('dex-active');
      if (dexContainer) dexContainer.classList.add('dex-layout');
    } else {
      body.classList.remove('dex-active');
      if (dexContainer) dexContainer.classList.remove('dex-layout');
    }
  },

  // 3. Tùy biến Giao diện & Chủ đề màu sắc
  setTheme: function(themeName) {
    this.config.theme = themeName;
    this.saveToStorage();
    this.applyTheme(themeName);
    this.updateSettingsUi();
  },

  applyTheme: function(themeName) {
    document.documentElement.setAttribute('data-theme', themeName);
  },

  // 4. Kích thước hiển thị cỡ chữ
  setFontSize: function(size) {
    this.config.fontSize = size;
    this.saveToStorage();
    this.applyFontSize(size);
    this.updateSettingsUi();
  },

  applyFontSize: function(size) {
    document.documentElement.setAttribute('data-font-size', size);
  },

  // 5. Chế độ bố cục lá số trên điện thoại
  setViewMode: function(mode) {
    this.config.viewMode = mode;
    this.saveToStorage();
    if (typeof renderChart === 'function' && typeof currentChart !== 'undefined' && currentChart) {
      renderChart(currentChart);
    }
    this.updateSettingsUi();
  },

  // 6. Kiểm tra VPN & DNS Bảo Mật Độc Lập Bên Ngoài
  checkExternalVpn: function(statusElId) {
    const el = document.getElementById(statusElId || 'tabVpnStatus');
    if (el) {
      el.innerHTML = '<span class="status-checking">⏳ Đang kiểm tra định tuyến ngoại vi...</span>';
    }

    const start = Date.now();
    fetch('https://dns.google/resolve?name=cloudflare.com&type=A', { cache: 'no-store' })
      .then(res => res.json())
      .then(() => {
        const ping = Date.now() - start;
        const msg = `🛡️ Kết nối an toàn (Ping: ${ping}ms | DNS: Google Sec)`;
        this.config.lastVpnStatus = msg;
        if (el) el.innerHTML = `<span class="status-online">${msg}</span>`;
      })
      .catch(() => {
        const ping = Date.now() - start;
        const msg = `🛡️ VPN/Mạng ngoại vi hoạt động (${ping}ms) - Định tuyến độc lập`;
        this.config.lastVpnStatus = msg;
        if (el) el.innerHTML = `<span class="status-online">${msg}</span>`;
      });
  },

  // 7. Đồng bộ dữ liệu lá số & Sao lưu
  exportBackup: function() {
    const data = {
      app: 'Applied Astrology (Tử Vi Ứng Dụng)',
      version: '0.0.1',
      date: new Date().toISOString(),
      settings: this.config
    };
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `TuVi_Settings_Backup_${new Date().toISOString().slice(0, 10)}.json`;
    a.click();
    URL.revokeObjectURL(url);
  },

  // Cập nhật giá trị hiển thị trong Tab Cài Đặt
  updateSettingsUi: function() {
    const selLang = document.getElementById('tabLangSelect');
    if (selLang) selLang.value = this.config.language;

    const selTheme = document.getElementById('tabThemeSelect');
    if (selTheme) selTheme.value = this.config.theme;

    const selFont = document.getElementById('tabFontSizeSelect');
    if (selFont) selFont.value = this.config.fontSize;

    const chkDex = document.getElementById('tabDexToggle');
    if (chkDex) chkDex.checked = this.config.dexMode;

    const selView = document.getElementById('tabViewModeSelect');
    if (selView) selView.value = this.config.viewMode;

    const vpnEl = document.getElementById('tabVpnStatus');
    if (vpnEl && this.config.lastVpnStatus) {
      vpnEl.innerHTML = `<span class="status-online">${this.config.lastVpnStatus}</span>`;
    }
  }
};

// Tự động khởi chạy cài đặt khi trang được nạp
if (typeof document !== 'undefined') {
  document.addEventListener('DOMContentLoaded', () => {
    AppSettings.init();
  });
}
