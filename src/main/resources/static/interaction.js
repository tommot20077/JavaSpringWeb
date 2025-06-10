// Modern Blog Interactions and Enhancements
// 現代化部落格互動效果與增強功能

(function() {
    'use strict';

    // 工具函數
    const utils = {
        // 防抖函數
        debounce: function(func, wait) {
            let timeout;
            return function executedFunction(...args) {
                const later = () => {
                    clearTimeout(timeout);
                    func(...args);
                };
                clearTimeout(timeout);
                timeout = setTimeout(later, wait);
            };
        },

        // 節流函數
        throttle: function(func, limit) {
            let inThrottle;
            return function(...args) {
                if (!inThrottle) {
                    func.apply(this, args);
                    inThrottle = true;
                    setTimeout(() => inThrottle = false, limit);
                }
            };
        },

        // 平滑滾動
        smoothScroll: function(target, duration = 1000) {
            const targetElement = document.querySelector(target);
            if (!targetElement) return;

            const targetPosition = targetElement.getBoundingClientRect().top + window.pageYOffset;
            const startPosition = window.pageYOffset;
            const distance = targetPosition - startPosition;
            let startTime = null;

            function animation(currentTime) {
                if (startTime === null) startTime = currentTime;
                const timeElapsed = currentTime - startTime;
                const run = ease(timeElapsed, startPosition, distance, duration);
                window.scrollTo(0, run);
                if (timeElapsed < duration) requestAnimationFrame(animation);
            }

            function ease(t, b, c, d) {
                t /= d / 2;
                if (t < 1) return c / 2 * t * t + b;
                t--;
                return -c / 2 * (t * (t - 2) - 1) + b;
            }

            requestAnimationFrame(animation);
        }
    };

    // 導航欄增強
    const navbar = {
        init: function() {
            const nav = document.querySelector('.navbar');
            if (!nav) return;

            // 滾動效果
            let lastScroll = 0;
            window.addEventListener('scroll', utils.throttle(() => {
                const currentScroll = window.pageYOffset;

                if (currentScroll > 50) {
                    nav.classList.add('scrolled');
                } else {
                    nav.classList.remove('scrolled');
                }

                // 隱藏/顯示導航欄
                if (currentScroll > lastScroll && currentScroll > 200) {
                    nav.style.transform = 'translateY(-100%)';
                } else {
                    nav.style.transform = 'translateY(0)';
                }

                lastScroll = currentScroll;
            }, 100));

            // 移動端選單優化
            const toggler = nav.querySelector('.navbar-toggler');
            const collapse = nav.querySelector('.navbar-collapse');

            if (toggler && collapse) {
                toggler.addEventListener('click', function() {
                    document.body.classList.toggle('menu-open');
                });

                // 點擊外部關閉選單
                document.addEventListener('click', function(e) {
                    if (!nav.contains(e.target) && collapse.classList.contains('show')) {
                        toggler.click();
                    }
                });
            }
        }
    };

    // 載入動畫
    const loadingEffects = {
        init: function() {
            // 頁面載入動畫
            window.addEventListener('load', () => {
                document.body.classList.add('loaded');

                // 元素漸入動畫
                const animateElements = document.querySelectorAll('.animate-fade-in');
                const observer = new IntersectionObserver((entries) => {
                    entries.forEach(entry => {
                        if (entry.isIntersecting) {
                            entry.target.classList.add('visible');
                            observer.unobserve(entry.target);
                        }
                    });
                }, { threshold: 0.1 });

                animateElements.forEach(el => observer.observe(el));
            });

            // 圖片延遲載入
            this.lazyLoadImages();
        },

        lazyLoadImages: function() {
            const images = document.querySelectorAll('img[data-src]');
            const imageObserver = new IntersectionObserver((entries, observer) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        const img = entry.target;
                        img.src = img.dataset.src;
                        img.classList.add('loaded');
                        img.removeAttribute('data-src');
                        imageObserver.unobserve(img);
                    }
                });
            });

            images.forEach(img => imageObserver.observe(img));
        }
    };

    // 文章卡片互動
    const articleCards = {
        init: function() {
            const cards = document.querySelectorAll('.article-item .card');

            cards.forEach(card => {
                // 滑鼠追蹤效果
                card.addEventListener('mousemove', this.handleMouseMove);
                card.addEventListener('mouseleave', this.handleMouseLeave);

                // 點擊波紋效果
                card.addEventListener('click', this.createRipple);
            });
        },

        handleMouseMove: function(e) {
            const card = e.currentTarget;
            const rect = card.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;

            card.style.setProperty('--mouse-x', `${x}px`);
            card.style.setProperty('--mouse-y', `${y}px`);
        },

        handleMouseLeave: function(e) {
            const card = e.currentTarget;
            card.style.removeProperty('--mouse-x');
            card.style.removeProperty('--mouse-y');
        },

        createRipple: function(e) {
            const card = e.currentTarget;
            const ripple = document.createElement('span');
            const rect = card.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            const x = e.clientX - rect.left - size / 2;
            const y = e.clientY - rect.top - size / 2;

            ripple.style.width = ripple.style.height = size + 'px';
            ripple.style.left = x + 'px';
            ripple.style.top = y + 'px';
            ripple.classList.add('ripple');

            card.appendChild(ripple);

            setTimeout(() => ripple.remove(), 600);
        }
    };

    // 表單增強
    const formEnhancements = {
        init: function() {
            // 輸入框浮動標籤
            const inputs = document.querySelectorAll('.form-control');
            inputs.forEach(input => {
                // 檢查初始值
                if (input.value) {
                    input.classList.add('has-value');
                }

                input.addEventListener('focus', () => {
                    input.parentElement.classList.add('focused');
                });

                input.addEventListener('blur', () => {
                    input.parentElement.classList.remove('focused');
                    if (input.value) {
                        input.classList.add('has-value');
                    } else {
                        input.classList.remove('has-value');
                    }
                });
            });

            // 密碼強度指示器
            this.passwordStrength();
        },

        passwordStrength: function() {
            const passwordInput = document.querySelector('input[type="password"]');
            if (!passwordInput) return;

            const strengthIndicator = document.createElement('div');
            strengthIndicator.className = 'password-strength';
            passwordInput.parentElement.appendChild(strengthIndicator);

            passwordInput.addEventListener('input', utils.debounce((e) => {
                const password = e.target.value;
                const strength = this.calculateStrength(password);

                strengthIndicator.className = `password-strength strength-${strength.level}`;
                strengthIndicator.textContent = strength.text;
            }, 300));
        },

        calculateStrength: function(password) {
            let strength = 0;
            const patterns = [
                /[a-z]/, // 小寫字母
                /[A-Z]/, // 大寫字母
                /[0-9]/, // 數字
                /[^A-Za-z0-9]/ // 特殊字符
            ];

            patterns.forEach(pattern => {
                if (pattern.test(password)) strength++;
            });

            if (password.length >= 8) strength++;
            if (password.length >= 12) strength++;

            const levels = ['weak', 'fair', 'good', 'strong', 'excellent'];
            const texts = ['弱', '一般', '良好', '強', '優秀'];

            const level = Math.min(Math.floor(strength * 0.8), 4);

            return {
                level: levels[level],
                text: texts[level]
            };
        }
    };

    // 搜尋功能增強
    const searchEnhancements = {
        init: function() {
            const searchInput = document.querySelector('.search-input');
            const searchForm = document.querySelector('.search-form');

            if (!searchInput || !searchForm) return;

            // 自動完成建議
            let suggestionsContainer = document.createElement('div');
            suggestionsContainer.className = 'search-suggestions';
            searchForm.appendChild(suggestionsContainer);

            searchInput.addEventListener('input', utils.debounce((e) => {
                const query = e.target.value.trim();
                if (query.length > 2) {
                    this.showSuggestions(query, suggestionsContainer);
                } else {
                    suggestionsContainer.innerHTML = '';
                    suggestionsContainer.classList.remove('active');
                }
            }, 300));

            // 搜尋歷史
            this.handleSearchHistory(searchInput);
        },

        showSuggestions: function(query, container) {
            // 模擬搜尋建議 (實際應該從後端獲取)
            const suggestions = [
                'JavaScript 教程',
                'CSS 動畫效果',
                'React 最佳實踐',
                '前端性能優化'
            ].filter(s => s.toLowerCase().includes(query.toLowerCase()));

            if (suggestions.length > 0) {
                container.innerHTML = suggestions.map(s =>
                    `<div class="suggestion-item">${s}</div>`
                ).join('');
                container.classList.add('active');

                // 點擊建議項
                container.querySelectorAll('.suggestion-item').forEach(item => {
                    item.addEventListener('click', function() {
                        searchInput.value = this.textContent;
                        container.innerHTML = '';
                        container.classList.remove('active');
                        searchForm.submit();
                    });
                });
            }
        },

        handleSearchHistory: function(input) {
            const history = JSON.parse(localStorage.getItem('searchHistory') || '[]');

            input.addEventListener('focus', () => {
                if (input.value === '' && history.length > 0) {
                    // 顯示搜尋歷史
                }
            });

            input.form.addEventListener('submit', () => {
                const query = input.value.trim();
                if (query && !history.includes(query)) {
                    history.unshift(query);
                    if (history.length > 10) history.pop();
                    localStorage.setItem('searchHistory', JSON.stringify(history));
                }
            });
        }
    };

    // 主題切換
    const themeToggle = {
        init: function() {
            const currentTheme = localStorage.getItem('theme') || 'dark';
            document.documentElement.setAttribute('data-theme', currentTheme);

            // 創建主題切換按鈕
            const toggleButton = document.createElement('button');
            toggleButton.className = 'theme-toggle';
            toggleButton.innerHTML = currentTheme === 'dark' ? '🌞' : '🌙';
            toggleButton.setAttribute('aria-label', '切換主題');
            document.body.appendChild(toggleButton);

            toggleButton.addEventListener('click', () => {
                const theme = document.documentElement.getAttribute('data-theme');
                const newTheme = theme === 'dark' ? 'light' : 'dark';

                document.documentElement.setAttribute('data-theme', newTheme);
                localStorage.setItem('theme', newTheme);
                toggleButton.innerHTML = newTheme === 'dark' ? '🌞' : '🌙';

                // 添加過渡動畫
                document.body.style.transition = 'background-color 0.3s ease, color 0.3s ease';
            });
        }
    };

    // 性能優化
    const performance = {
        init: function() {
            // 預加載關鍵資源
            this.preloadResources();

            // 優化滾動性能
            this.optimizeScroll();
        },

        preloadResources: function() {
            const preloadLinks = [
                { href: '/common.css', as: 'style' },
                { href: '/images/hero-bg.jpg', as: 'image' }
            ];

            preloadLinks.forEach(link => {
                const preloadLink = document.createElement('link');
                preloadLink.rel = 'preload';
                preloadLink.href = link.href;
                preloadLink.as = link.as;
                document.head.appendChild(preloadLink);
            });
        },

        optimizeScroll: function() {
            let ticking = false;

            function updateScrollEffects() {
                // 滾動相關的效果更新
                ticking = false;
            }

            window.addEventListener('scroll', () => {
                if (!ticking) {
                    window.requestAnimationFrame(updateScrollEffects);
                    ticking = true;
                }
            });
        }
    };

    // 初始化所有功能
    document.addEventListener('DOMContentLoaded', function() {
        navbar.init();
        loadingEffects.init();
        articleCards.init();
        formEnhancements.init();
        searchEnhancements.init();
        themeToggle.init();
        performance.init();

        // 添加頁面載入完成類
        setTimeout(() => {
            document.body.classList.add('page-loaded');
        }, 100);
    });

    // 暴露工具函數供外部使用
    window.blogUtils = utils;

})();