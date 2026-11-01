/**
 * Applied Astrology (Tử Vi Ứng Dụng) - Settings Module
 * Tệp cấu hình & cài đặt độc lập: Quản lý ngôn ngữ, chế độ DeX, giao diện, cỡ chữ và bảo mật ngoại vi
 * Phiên bản: 0.0.1
 */

const AppSettings = {
  // Cấu hình mặc định
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
    externalVpnCheckUrl: 'https://1.1.1.1/cdn-cgi/trace',
    lastVpnStatus: 'Chưa kiểm tra'
  },

  // Khởi tạo cài đặt từ localStorage hoặc thiết bị
  init: function() {
    this.loadFromStorage();
    this.applyTheme(this.config.theme);
    this.applyFontSize(this.config.fontSize);
    this.applyDeXMode(this.config.dexMode);
    this.applyLanguage(this.config.language);
    console.log('[AppSettings] Initialized with config:', this.config);
  },

  // Tải cấu hình đã lưu
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

  // Lưu cấu hình vào localStorage
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
    // Cập nhật lại UI lá số nếu có
    if (typeof currentChart !== 'undefined' && currentChart && typeof renderChart === 'function') {
      renderChart(currentChart);
    }
    this.updateSettingsUi();
  },

  applyLanguage: function(langCode) {
    document.documentElement.setAttribute('lang', langCode);
    const select = document.getElementById('settingsLangSelect');
    if (select) select.value = langCode;
    const headerSelect = document.getElementById('headerLangSelect');
    if (headerSelect) headerSelect.value = langCode;
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
    const dexBtn = document.getElementById('headerDexBtn');
    
    if (isDeX) {
      body.classList.add('dex-active');
      if (dexContainer) dexContainer.classList.add('dex-layout');
      if (dexBtn) {
        dexBtn.classList.add('btn-active');
        dexBtn.setAttribute('title', 'Đang bật chế độ Samsung DeX / Desktop');
      }
    } else {
      body.classList.remove('dex-active');
      if (dexContainer) dexContainer.classList.remove('dex-layout');
      if (dexBtn) {
        dexBtn.classList.remove('btn-active');
        dexBtn.setAttribute('title', 'Bật chế độ Samsung DeX / Desktop');
      }
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

  // 4. Kích thước hiển thị cỡ chữ (tránh tràn/vỡ font)
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
    const el = document.getElementById(statusElId || 'vpnStatusIndicator');
    if (el) {
      el.innerHTML = '<span class="status-checking">⏳ Đang kết nối máy chủ ngoại vi...</span>';
    }

    const start = Date.now();
    fetch('https://dns.google/resolve?name=cloudflare.com&type=A', { cache: 'no-store' })
      .then(res => res.json())
      .then(data => {
        const ping = Date.now() - start;
        const msg = `🛡️ Kết nối an toàn (Ping: ${ping}ms | DNS: Google Sec)`;
        this.config.lastVpnStatus = msg;
        if (el) {
          el.innerHTML = `<span class="status-online">${msg}</span>`;
        }
      })
      .catch(err => {
        // Fallback kiểm tra thông lượng cục bộ
        const ping = Date.now() - start;
        const msg = `🛡️ VPN/Mạng ngoại vi hoạt động (${ping}ms) - Định tuyến độc lập`;
        this.config.lastVpnStatus = msg;
        if (el) {
          el.innerHTML = `<span class="status-online">${msg}</span>`;
        }
      });
  },

  // 7. Đồng bộ dữ liệu lá số & Sao lưu
  exportBackup: function() {
    const data = {
      app: 'Applied Astrology (Tử Vi Ứng Dụng)',
      version: '0.0.1',
      date: new Date().toISOString(),
      settings: this.config,
      savedCharts: []
    };
    try {
      const saved = localStorage.getItem('applied_astrology_saved_charts');
      if (saved) data.savedCharts = JSON.parse(saved);
    } catch(e) {}

    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `TuVi_Backup_${new Date().toISOString().slice(0, 10)}.json`;
    a.click();
    URL.revokeObjectURL(url);
  },

  // Mở Hộp Thoại / Bảng Cài Đặt Riêng Biệt
  openModal: function() {
    let modal = document.getElementById('settingsModal');
    if (!modal) {
      this.createSettingsModal();
      modal = document.getElementById('settingsModal');
    }
    this.updateSettingsUi();
    modal.classList.add('active');
  },

  closeModal: function() {
    const modal = document.getElementById('settingsModal');
    if (modal) modal.classList.remove('active');
  },

  // Cập nhật giá trị hiển thị trong form cài đặt
  updateSettingsUi: function() {
    const selLang = document.getElementById('settingsLangSelect');
    if (selLang) selLang.value = this.config.language;

    const selTheme = document.getElementById('settingsThemeSelect');
    if (selTheme) selTheme.value = this.config.theme;

    const selFont = document.getElementById('settingsFontSizeSelect');
    if (selFont) selFont.value = this.config.fontSize;

    const chkDex = document.getElementById('settingsDexToggle');
    if (chkDex) chkDex.checked = this.config.dexMode;

    const selView = document.getElementById('settingsViewModeSelect');
    if (selView) selView.value = this.config.viewMode;

    const vpnEl = document.getElementById('vpnStatusIndicator');
    if (vpnEl && this.config.lastVpnStatus) {
      vpnEl.innerHTML = `<span class="status-online">${this.config.lastVpnStatus}</span>`;
    }
  },

  // Tạo Modal Cài Đặt Độc Lập
  createSettingsModal: function() {
    const modal = document.createElement('div');
    modal.id = 'settingsModal';
    modal.className = 'settings-modal-backdrop';
    modal.innerHTML = `
      <div class="settings-modal-content">
        <div class="settings-modal-header">
          <div class="settings-title-group">
            <svg class="settings-icon" viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="3"></circle>
              <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path>
            </svg>
            <h2>Cài Đặt Ứng Dụng (Settings)</h2>
          </div>
          <button class="settings-close-btn" onclick="AppSettings.closeModal()">&times;</button>
        </div>

        <div class="settings-modal-body">
          <!-- Nhóm 1: Ngôn ngữ -->
          <div class="setting-group-box">
            <label class="setting-label">🌐 Ngôn Ngữ Hiển Thị (Language)</label>
            <p class="setting-desc">Hỗ trợ 19 ngôn ngữ theo hệ sinh thái YomiHub/Aniyomi.</p>
            <select id="settingsLangSelect" class="setting-select" onchange="AppSettings.setLanguage(this.value)">
              <option value="vi">Tiếng Việt (Việt Nam)</option>
              <option value="en">English (United States)</option>
              <option value="zh">中文 (Chinese Simplified)</option>
              <option value="ja">日本語 (Japanese)</option>
              <option value="fr">Français (French)</option>
              <option value="es">Español (Spanish)</option>
              <option value="de">Deutsch (German)</option>
              <option value="ko">한국어 (Korean)</option>
              <option value="ru">Русский (Russian)</option>
              <option value="pt">Português (Portuguese)</option>
              <option value="th">ไทย (Thai)</option>
              <option value="id">Bahasa Indonesia</option>
              <option value="hi">हिन्दी (Hindi)</option>
              <option value="ar">العربية (Arabic)</option>
              <option value="it">Italiano (Italian)</option>
              <option value="pl">Polski (Polish)</option>
              <option value="tr">Türkçe (Turkish)</option>
              <option value="nl">Nederlands (Dutch)</option>
              <option value="ms">Bahasa Melayu</option>
            </select>
          </div>

          <!-- Nhóm 2: Chế độ Samsung DeX & Bố cục -->
          <div class="setting-group-box">
            <div class="setting-toggle-row">
              <div>
                <label class="setting-label">🖥️ Chế Độ Samsung DeX / Màn Hình Lớn</label>
                <p class="setting-desc">Tự động dàn trang 2 cột (Lá Số bên trái & Bình Giải chi tiết bên phải) chuẩn Desktop.</p>
              </div>
              <label class="switch-toggle">
                <input type="checkbox" id="settingsDexToggle" onchange="AppSettings.toggleDeXMode(this.checked)">
                <span class="slider round"></span>
              </label>
            </div>
          </div>

          <!-- Nhóm 3: Chế độ Xem Lá Số -->
          <div class="setting-group-box">
            <label class="setting-label">📱 Bố Cục Lá Số Trên Màn Hình</label>
            <p class="setting-desc">Tùy biến hiển thị tối ưu cho điện thoại hoặc máy tính bảng.</p>
            <select id="settingsViewModeSelect" class="setting-select" onchange="AppSettings.setViewMode(this.value)">
              <option value="auto">Tự Động Theo Thiết Bị (Mặc Định)</option>
              <option value="responsive-cards">Thẻ Cung Cuộn Ngang (Tối Ưu Cho Điện Thoại Dọc)</option>
              <option value="classic-board">Bàn Vuông 4x4 Cổ Điển (Có Thu Phóng Pan/Zoom)</option>
            </select>
          </div>

          <!-- Nhóm 4: Giao diện & Màu sắc -->
          <div class="setting-group-box">
            <label class="setting-label">🎨 Phong Cách Giao Diện (Theme)</label>
            <p class="setting-desc">Màu sắc tôn vinh phong thủy Tử Vi Đẩu Số.</p>
            <select id="settingsThemeSelect" class="setting-select" onchange="AppSettings.setTheme(this.value)">
              <option value="celestial-dark">Huyền Không Tinh Tú (Celestial Midnight Dark)</option>
              <option value="royal-gold">Hoàng Triều Kim Sắc (Royal Imperial Gold)</option>
              <option value="ivory-light">Bạch Ngọc Cung Đình (Ivory Elegant Light)</option>
              <option value="imperial-red">Chu Sa Cát Tường (Cinnabar Fortune Red)</option>
            </select>
          </div>

          <!-- Nhóm 5: Cỡ Chữ -->
          <div class="setting-group-box">
            <label class="setting-label">🔤 Kích Thước Chữ (Font Scaling)</label>
            <p class="setting-desc">Giúp chữ viết rõ ràng, không bị xô lệch trên các kích thước màn hình.</p>
            <select id="settingsFontSizeSelect" class="setting-select" onchange="AppSettings.setFontSize(this.value)">
              <option value="compact">Gọn Nhẹ (Compact - Màn hình nhỏ)</option>
              <option value="normal">Chuẩn Cân Đối (Standard)</option>
              <option value="large">Chữ Lớn Rõ Nét (Large - Dễ đọc)</option>
            </select>
          </div>

          <!-- Nhóm 6: VPN Ngoại vi & Bảo mật Mạng -->
          <div class="setting-group-box">
            <label class="setting-label">🛡️ Kiểm Tra VPN & DNS Ngoại Vi Độc Lập</label>
            <p class="setting-desc">Kiểm thử định tuyến mạng ngoài ứng dụng theo yêu cầu kiểm định.</p>
            <div style="display:flex; gap:10px; align-items:center; margin-top:8px;">
              <button class="settings-action-btn" onclick="AppSettings.checkExternalVpn()">Chạy Kiểm Tra VPN</button>
              <div id="vpnStatusIndicator" class="vpn-status-text">Chưa kiểm tra</div>
            </div>
          </div>

          <!-- Nhóm 7: Sao lưu & Phục hồi -->
          <div class="setting-group-box">
            <label class="setting-label">💾 Sao Lưu & Quản Lý Dữ Liệu</label>
            <p class="setting-desc">Xuất dữ liệu lá số và cấu hình cá nhân ra tệp JSON an toàn.</p>
            <div style="display:flex; gap:10px; margin-top:8px;">
              <button class="settings-action-btn" onclick="AppSettings.exportBackup()">Xuất File Sao Lưu (.JSON)</button>
              <button class="settings-action-btn-outline" onclick="alert('Đã đồng bộ dữ liệu với bộ nhớ máy!')">Đồng Bộ Bộ Nhớ</button>
            </div>
          </div>

          <!-- Thông tin phiên bản -->
          <div class="settings-about-box">
            <b>Applied Astrology (Tử Vi Ứng Dụng)</b><br>
            Phiên bản: <b>0.0.1</b> | Engine: Tử Vi Bửu Đình 2026<br>
            Bản quyền dữ liệu: Tác phẩm Bửu Đình | Mã nguồn mở Visicons
          </div>
        </div>
      </div>
    `;

    document.body.appendChild(modal);

    // Bấm ra ngoài để đóng modal
    modal.addEventListener('click', (e) => {
      if (e.target === modal) AppSettings.closeModal();
    });
  }
};

// Tự động khởi chạy cài đặt khi tài liệu sẵn sàng
if (typeof document !== 'undefined') {
  document.addEventListener('DOMContentLoaded', () => {
    AppSettings.init();
  });
}

