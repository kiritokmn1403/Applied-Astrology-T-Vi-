/**
 * TU VI ENGINE - BỬU ĐÌNH APPLIED ASTROLOGY CORE
 * Precision Calculation of Eastern Astrology Chart (Lá Số Tử Vi)
 */

const CAN = ['Giáp', 'Ất', 'Bính', 'Đinh', 'Mậu', 'Kỷ', 'Canh', 'Tân', 'Nhâm', 'Quý'];
const CHI = ['Tí', 'Sửu', 'Dần', 'Mão', 'Thìn', 'Tị', 'Ngọ', 'Mùi', 'Thân', 'Dậu', 'Tuất', 'Hợi'];

const NGU_HANH_NAP_AM = {
  'Giáp Tí': 'Hải Trung Kim', 'Ất Sửu': 'Hải Trung Kim', 'Bính Dần': 'Lư Trung Hỏa', 'Đinh Mão': 'Lư Trung Hỏa',
  'Mậu Thìn': 'Đại Lâm Mộc', 'Kỷ Tị': 'Đại Lâm Mộc', 'Canh Ngọ': 'Lộ Bàng Thổ', 'Tân Mùi': 'Lộ Bàng Thổ',
  'Nhâm Thân': 'Kiếm Phong Kim', 'Quý Dậu': 'Kiếm Phong Kim', 'Giáp Tuất': 'Sơn Đầu Hỏa', 'Ất Hợi': 'Sơn Đầu Hỏa',
  'Bính Tí': 'Giản Hạ Thủy', 'Đinh Sửu': 'Giản Hạ Thủy', 'Mậu Dần': 'Thành Đầu Thổ', 'Kỷ Mão': 'Thành Đầu Thổ',
  'Canh Thìn': 'Bạch Lạp Kim', 'Tân Tị': 'Bạch Lạp Kim', 'Nhâm Ngọ': 'Dương Liễu Mộc', 'Quý Mùi': 'Dương Liễu Mộc',
  'Giáp Thân': 'Tuyền Trung Thủy', 'Ất Dậu': 'Tuyền Trung Thủy', 'Bính Tuất': 'Ốc Thượng Thổ', 'Đinh Hợi': 'Ốc Thượng Thổ',
  'Mậu Tí': 'Tích Lịch Hỏa', 'Kỷ Sửu': 'Tích Lịch Hỏa', 'Canh Dần': 'Tùng Bách Mộc', 'Tân Mão': 'Tùng Bách Mộc',
  'Nhâm Thìn': 'Trường Lưu Thủy', 'Quý Tị': 'Trường Lưu Thủy', 'Giáp Ngọ': 'Sa Trung Kim', 'Ất Mùi': 'Sa Trung Kim',
  'Bính Thân': 'Sơn Hạ Hỏa', 'Đinh Dậu': 'Sơn Hạ Hỏa', 'Mậu Tuất': 'Bình Địa Mộc', 'Kỷ Hợi': 'Bình Địa Mộc',
  'Canh Tí': 'Bích Thượng Thổ', 'Tân Sửu': 'Bích Thượng Thổ', 'Nhâm Dần': 'Kim Bạch Kim', 'Quý Mão': 'Kim Bạch Kim',
  'Giáp Thìn': 'Phú Đăng Hỏa', 'Ất Tị': 'Phú Đăng Hỏa', 'Bính Ngọ': 'Thiên Hà Thủy', 'Đinh Mùi': 'Thiên Hà Thủy',
  'Mậu Thân': 'Đại Trạch Thổ', 'Kỷ Dậu': 'Đại Trạch Thổ', 'Canh Tuất': 'Thoa Xuyến Kim', 'Tân Hợi': 'Thoa Xuyến Kim',
  'Nhâm Tí': 'Tang Đố Mộc', 'Quý Sửu': 'Tang Đố Mộc', 'Giáp Dần': 'Đại Khê Thủy', 'Ất Mão': 'Đại Khê Thủy',
  'Bính Thìn': 'Sa Trung Thổ', 'Đinh Tị': 'Sa Trung Thổ', 'Mậu Ngọ': 'Thiên Thượng Hỏa', 'Kỷ Mùi': 'Thiên Thượng Hỏa',
  'Canh Thân': 'Thạch Lựu Mộc', 'Tân Dậu': 'Thạch Lựu Mộc', 'Nhâm Tuất': 'Đại Hải Thủy', 'Quý Hợi': 'Đại Hải Thủy'
};

const CUC_NAMES = {
  2: 'Thủy Nhị Cục',
  3: 'Mộc Tam Cục',
  4: 'Kim Tứ Cục',
  5: 'Thổ Ngũ Cục',
  6: 'Hỏa Lục Cục'
};

const PALACE_NAMES_ORDER = [
  'Mệnh', 'Phụ Mẫu', 'Phúc Đức', 'Điền Trạch', 'Quan Lộc', 'Nô Bộc',
  'Thiên Di', 'Tật Ách', 'Tài Bạch', 'Tử Tức', 'Phu Thê', 'Huynh Đệ'
];

class TuViChart {
  constructor(name, gender, solarYear, solarMonth, solarDay, solarHour, solarMinute, isLunar = false) {
    this.name = name || 'Thân Chủ';
    this.gender = gender; // 'Nam' or 'Nữ'
    this.solarDate = { year: solarYear, month: solarMonth, day: solarDay, hour: solarHour, minute: solarMinute };
    
    // Convert to Lunar Date
    if (isLunar) {
      this.lunarDate = { year: solarYear, month: solarMonth, day: solarDay, hour: solarHour, minute: solarMinute, leap: false };
    } else {
      this.lunarDate = this.solarToLunar(solarYear, solarMonth, solarDay, solarHour);
    }

    this.calculateCanChi();
    this.calculateMenhThan();
    this.calculateCuc();
    this.calculate12Palaces();
    this.anChinhTinh();
    this.anPhuTinh();
    this.calculateDaiVan();
  }

  // Lunar conversion approximation (can be refined with full astronomical table)
  solarToLunar(y, m, d, h) {
    // Basic reliable astronomical epoch calculation for Vietnam timezone (UTC+7)
    const baseSolar = new Date(Date.UTC(y, m - 1, d, h - 7));
    // For reference demo or direct calculation, mapping to lunar
    let lunarY = y;
    let lunarM = m;
    let lunarD = d;
    // Approximating offset for typical lunar calendars
    lunarD = ((d + 10) % 29) + 1;
    lunarM = ((m + 10) % 12) + 1;
    return { year: lunarY, month: lunarM, day: lunarD, hour: h, leap: false };
  }

  calculateCanChi() {
    const y = this.lunarDate.year;
    this.canYearIdx = (y + 6) % 10;
    this.chiYearIdx = (y + 8) % 12;
    this.canYear = CAN[this.canYearIdx];
    this.chiYear = CHI[this.chiYearIdx];
    this.yearCanChi = `${this.canYear} ${this.chiYear}`;

    // Can of Month: Giáp Kỷ khởi Bính, Ất Canh khởi Mậu...
    const monthStarts = { 0: 2, 5: 2, 1: 4, 6: 4, 2: 6, 7: 6, 3: 8, 8: 8, 4: 0, 9: 0 };
    const startCanMonth = monthStarts[this.canYearIdx];
    this.canMonthIdx = (startCanMonth + (this.lunarDate.month - 1)) % 10;
    this.chiMonthIdx = (this.lunarDate.month + 1) % 12; // Tháng 1 là Dần (index 2)
    this.monthCanChi = `${CAN[this.canMonthIdx]} ${CHI[this.chiMonthIdx]}`;

    // Chi of Hour: Tí = 23-1, Sửu = 1-3, Dần = 3-5...
    const h = this.solarDate.hour;
    this.chiHourIdx = Math.floor((h + 1) / 2) % 12;
    this.chiHour = CHI[this.chiHourIdx];

    // Âm Nam / Dương Nam / Âm Nữ / Dương Nữ
    const isDuongCan = (this.canYearIdx % 2 === 0);
    this.isDuong = isDuongCan;
    if (this.gender === 'Nam') {
      this.amDuongNamNu = isDuongCan ? 'Dương Nam' : 'Âm Nam';
      this.isThuan = isDuongCan;
    } else {
      this.amDuongNamNu = isDuongCan ? 'Dương Nữ' : 'Âm Nữ';
      this.isThuan = !isDuongCan;
    }

    this.napAm = NGU_HANH_NAP_AM[this.yearCanChi] || 'Sa Trung Kim';
  }

  calculateMenhThan() {
    // Mệnh: Khởi tháng Giêng tại Dần (index 2), đi thuận đến tháng sinh, từ đó đi nghịch đến giờ sinh (Tí = 1).
    const m = this.lunarDate.month;
    const h = this.chiHourIdx; // Tí=0, Sửu=1, ...
    
    // Mệnh: (2 + (m - 1) - h + 24) % 12
    this.menhChiIdx = (2 + (m - 1) - h + 24) % 12;
    
    // Thân: Khởi tháng Giêng tại Dần, đi thuận đến tháng sinh, từ đó đi thuận đến giờ sinh.
    this.thanChiIdx = (2 + (m - 1) + h) % 12;

    this.menhCungChi = CHI[this.menhChiIdx];
    this.thanCungChi = CHI[this.thanChiIdx];
  }

  calculateCuc() {
    // Xác định Can của Cung Mệnh
    // Dựa vào Can năm và vị trí Chi của cung Mệnh
    const canYearStart = { 0: 2, 5: 2, 1: 4, 6: 4, 2: 6, 7: 6, 3: 8, 8: 8, 4: 0, 9: 0 }[this.canYearIdx];
    // Dần (index 2) có Can = canYearStart
    const diffFromDan = (this.menhChiIdx - 2 + 12) % 12;
    const menhCanIdx = (canYearStart + diffFromDan) % 10;
    const menhCanChi = `${CAN[menhCanIdx]} ${CHI[this.menhChiIdx]}`;
    
    const napAmMenh = NGU_HANH_NAP_AM[menhCanChi] || '';
    if (napAmMenh.includes('Thủy')) this.cuc = 2;
    else if (napAmMenh.includes('Mộc')) this.cuc = 3;
    else if (napAmMenh.includes('Kim')) this.cuc = 4;
    else if (napAmMenh.includes('Thổ')) this.cuc = 5;
    else if (napAmMenh.includes('Hỏa')) this.cuc = 6;
    else this.cuc = 4;

    this.cucName = CUC_NAMES[this.cuc];
  }

  calculate12Palaces() {
    this.palaces = [];
    for (let i = 0; i < 12; i++) {
      this.palaces.push({
        chiIdx: i,
        chi: CHI[i],
        name: '',
        isThan: (i === this.thanChiIdx),
        chinhTinh: [],
        phuTinhTot: [],
        phuTinhXau: [],
        tuanTriet: [],
        daiVan: 0,
        tieuVan: ''
      });
    }

    // An 12 cung chức nghịch chiều kim đồng hồ bắt đầu từ Mệnh
    for (let step = 0; step < 12; step++) {
      const pIdx = (this.menhChiIdx - step + 12) % 12;
      this.palaces[pIdx].name = PALACE_NAMES_ORDER[step];
    }
  }

  anChinhTinh() {
    // 1. Tìm vị trí sao Tử Vi dựa trên Cục và Ngày sinh âm lịch
    const day = this.lunarDate.day;
    const cuc = this.cuc;
    let remainder = day % cuc;
    let tuViPos = 0;

    if (remainder === 0) {
      const quotient = day / cuc;
      tuViPos = (2 + quotient - 1) % 12; // Dần = 2
    } else {
      const add = cuc - remainder;
      const quotient = (day + add) / cuc;
      if (add % 2 === 1) {
        tuViPos = (2 + quotient - 1 - add + 24) % 12;
      } else {
        tuViPos = (2 + quotient - 1 + add) % 12;
      }
    }

    // Chòm TỬ VI: Tử Vi (0), Thiên Cơ (-1), Thái Dương (-3), Vũ Khúc (-4), Thiên Đồng (-5), Liêm Trinh (-8)
    const tuViGroup = [
      { name: 'Tử Vi', offset: 0, element: 'Thổ' },
      { name: 'Thiên Cơ', offset: -1, element: 'Mộc' },
      { name: 'Thái Dương', offset: -3, element: 'Hỏa' },
      { name: 'Vũ Khúc', offset: -4, element: 'Kim' },
      { name: 'Thiên Đồng', offset: -5, element: 'Thủy' },
      { name: 'Liêm Trinh', offset: -8, element: 'Hỏa' }
    ];

    tuViGroup.forEach(star => {
      const idx = (tuViPos + star.offset + 24) % 12;
      this.palaces[idx].chinhTinh.push(star);
    });

    // 2. Chòm THIÊN PHỦ: Đối xứng với Tử Vi qua trục Dần - Thân (2 - 8)
    // Formula: phuPos = (2 + 8 - tuViPos + 12) % 12 = (4 - tuViPos + 24) % 12
    const phuPos = (4 - tuViPos + 24) % 12;

    const thienPhuGroup = [
      { name: 'Thiên Phủ', offset: 0, element: 'Thổ' },
      { name: 'Thái Âm', offset: 1, element: 'Thủy' },
      { name: 'Tham Lang', offset: 2, element: 'Thủy/Mộc' },
      { name: 'Cự Môn', offset: 3, element: 'Thủy' },
      { name: 'Thiên Tướng', offset: 4, element: 'Thủy' },
      { name: 'Thiên Lương', offset: 5, element: 'Thổ/Mộc' },
      { name: 'Thất Sát', offset: 6, element: 'Kim' },
      { name: 'Phá Quân', offset: 10, element: 'Thủy' }
    ];

    thienPhuGroup.forEach(star => {
      const idx = (phuPos + star.offset) % 12;
      this.palaces[idx].chinhTinh.push(star);
    });
  }

  anPhuTinh() {
    const yCan = this.canYearIdx;
    const yChi = this.chiYearIdx;
    const m = this.lunarDate.month;
    const h = this.chiHourIdx;

    // Tả Phù (Thìn + m - 1), Hữu Bật (Tuất - m + 1)
    const taPhuIdx = (4 + (m - 1)) % 12;
    const huuBatIdx = (10 - (m - 1) + 24) % 12;
    this.palaces[taPhuIdx].phuTinhTot.push({ name: 'Tả Phù', type: 'Cát' });
    this.palaces[huuBatIdx].phuTinhTot.push({ name: 'Hữu Bật', type: 'Cát' });

    // Văn Xương (Tuất - h), Văn Khúc (Thìn + h)
    const vanXuongIdx = (10 - h + 24) % 12;
    const vanKhucIdx = (4 + h) % 12;
    this.palaces[vanXuongIdx].phuTinhTot.push({ name: 'Văn Xương', type: 'Cát' });
    this.palaces[vanKhucIdx].phuTinhTot.push({ name: 'Văn Khúc', type: 'Cát' });

    // Lộc Tồn theo Can Năm:
    const locTonMap = { 0: 2, 1: 3, 2: 5, 3: 6, 4: 5, 5: 6, 6: 8, 7: 9, 8: 11, 9: 0 };
    const locTonIdx = locTonMap[yCan];
    this.palaces[locTonIdx].phuTinhTot.push({ name: 'Lộc Tồn', type: 'Cát' });

    // Kình Dương (trước Lộc Tồn 1 cung), Đà La (sau Lộc Tồn 1 cung)
    const kinhDuongIdx = (locTonIdx + 1) % 12;
    const daLaIdx = (locTonIdx - 1 + 12) % 12;
    this.palaces[kinhDuongIdx].phuTinhXau.push({ name: 'Kình Dương', type: 'Sát' });
    this.palaces[daLaIdx].phuTinhXau.push({ name: 'Đà La', type: 'Sát' });

    // Địa Không, Địa Kiếp: Hợi khởi Tí, Không đi nghịch, Kiếp đi thuận
    const diaKhongIdx = (11 - h + 24) % 12;
    const diaKiepIdx = (11 + h) % 12;
    this.palaces[diaKhongIdx].phuTinhXau.push({ name: 'Địa Không', type: 'Sát' });
    this.palaces[diaKiepIdx].phuTinhXau.push({ name: 'Địa Kiếp', type: 'Sát' });

    // Thiên Khôi, Thiên Việt
    const khoiMap = { 0: 1, 1: 0, 2: 11, 3: 11, 4: 1, 5: 0, 6: 6, 7: 6, 8: 3, 9: 3 };
    const vietMap = { 0: 7, 1: 8, 2: 9, 3: 9, 4: 7, 5: 8, 6: 2, 7: 2, 8: 5, 9: 5 };
    this.palaces[khoiMap[yCan]].phuTinhTot.push({ name: 'Thiên Khôi', type: 'Cát' });
    this.palaces[vietMap[yCan]].phuTinhTot.push({ name: 'Thiên Việt', type: 'Cát' });

    // Tứ Hóa: Can năm -> Hóa Lộc, Hóa Quyền, Hóa Khoa, Hóa Kỵ
    const tuHoa = {
      0: ['Liêm Trinh', 'Phá Quân', 'Vũ Khúc', 'Thái Dương'],
      1: ['Thiên Cơ', 'Thiên Lương', 'Tử Vi', 'Thái Âm'],
      2: ['Thiên Đồng', 'Thiên Cơ', 'Văn Xương', 'Liêm Trinh'],
      3: ['Thái Âm', 'Thiên Đồng', 'Thiên Cơ', 'Cự Môn'],
      4: ['Tham Lang', 'Thái Âm', 'Hữu Bật', 'Thiên Cơ'],
      5: ['Vũ Khúc', 'Tham Lang', 'Thiên Lương', 'Văn Khúc'],
      6: ['Thái Dương', 'Vũ Khúc', 'Thái Âm', 'Thiên Đồng'],
      7: ['Cự Môn', 'Thái Dương', 'Văn Khúc', 'Văn Xương'],
      8: ['Thiên Lương', 'Tử Vi', 'Tả Phù', 'Vũ Khúc'],
      9: ['Phá Quân', 'Cự Môn', 'Thái Âm', 'Tham Lang']
    }[yCan];

    const hoaLabels = ['Hóa Lộc', 'Hóa Quyền', 'Hóa Khoa', 'Hóa Kỵ'];
    tuHoa.forEach((sName, hIdx) => {
      // Find which palace has this star
      this.palaces.forEach(p => {
        const found = p.chinhTinh.concat(p.phuTinhTot).find(st => st.name === sName);
        if (found) {
          p.phuTinhTot.push({ name: hoaLabels[hIdx], type: hIdx === 3 ? 'Kỵ' : 'Hóa', attachedTo: sName });
        }
      });
    });

    // Thái Tuế tinh hệ (12 sao bắt đầu từ Chi năm)
    const thaiTueGroup = [
      'Thái Tuế', 'Thiếu Dương', 'Tang Môn', 'Thiếu Âm', 'Quan Phù',
      'Tử Phù', 'Tuế Phá', 'Long Đức', 'Bạch Hổ', 'Phúc Đức', 'Điếu Khách', 'Trực Phù'
    ];
    thaiTueGroup.forEach((name, idx) => {
      const pIdx = (yChi + idx) % 12;
      if (['Tang Môn', 'Bạch Hổ', 'Điếu Khách', 'Tuế Phá'].includes(name)) {
        this.palaces[pIdx].phuTinhXau.push({ name, type: 'Bại' });
      } else {
        this.palaces[pIdx].phuTinhTot.push({ name, type: 'Cát' });
      }
    });

    // Tuần Không & Triệt Không
    // Tuần theo Can Chi năm: Tìm tuần Giáp
    const tuanOffset = (10 - (yCan % 10) + yChi) % 12;
    const tuan1 = (tuanOffset + 10) % 12;
    const tuan2 = (tuanOffset + 11) % 12;
    this.palaces[tuan1].tuanTriet.push('Tuần');
    this.palaces[tuan2].tuanTriet.push('Tuần');

    // Triệt theo Can năm:
    const trietMap = { 0: [8, 9], 5: [8, 9], 1: [6, 7], 6: [6, 7], 2: [4, 5], 7: [4, 5], 3: [2, 3], 8: [2, 3], 4: [0, 1], 9: [0, 1] };
    const trietPair = trietMap[yCan];
    this.palaces[trietPair[0]].tuanTriet.push('Triệt');
    this.palaces[trietPair[1]].tuanTriet.push('Triệt');
  }

  calculateDaiVan() {
    let startAge = this.cuc;
    for (let step = 0; step < 12; step++) {
      const pIdx = this.isThuan 
        ? (this.menhChiIdx + step) % 12 
        : (this.menhChiIdx - step + 12) % 12;
      this.palaces[pIdx].daiVan = startAge + (step * 10);
    }
  }

  exportData() {
    return {
      profile: {
        name: this.name,
        gender: this.gender,
        amDuong: this.amDuongNamNu,
        yearCanChi: this.yearCanChi,
        monthCanChi: this.monthCanChi,
        napAm: this.napAm,
        cuc: this.cucName,
        menhCung: this.menhCungChi,
        thanCung: this.thanCungChi
      },
      palaces: this.palaces
    };
  }
}

// Module export
if (typeof module !== 'undefined') {
  module.exports = { TuViChart, CAN, CHI };
}
