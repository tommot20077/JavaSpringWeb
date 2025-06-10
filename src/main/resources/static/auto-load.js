// Auto-load functionality for article lists
(function() {
    'use strict';

    let isLoading = false;
    let currentPage = 1;
    let totalPages = 1;
    let autoLoadEnabled = false;

    // Initialize auto-load functionality
    function initAutoLoad() {
        // Check if we're on article or user_detail page
        const isArticlePage = window.location.pathname === '/article';
        const isUserDetailPage = window.location.pathname.includes('/user/');

        if (!isArticlePage && !isUserDetailPage) return;

        // Get total pages from pagination
        const paginationItems = document.querySelectorAll('.pagination .page-item');
        if (paginationItems.length > 0) {
            const lastPageLink = document.querySelector('.pagination .page-item:nth-last-child(1) .page-link');
            if (lastPageLink) {
                const href = lastPageLink.getAttribute('href');
                const match = href?.match(/page=(\d+)/);
                if (match) {
                    totalPages = parseInt(match[1]);
                }
            }
        }

        // Get current page
        const activePage = document.querySelector('.pagination .page-item.active .page-link');
        if (activePage) {
            currentPage = parseInt(activePage.textContent);
        }

        // Create auto-load toggle button
        createAutoLoadToggle();

        // Check view mode and enable/disable auto-load
        checkViewMode();

        // Listen for scroll events
        window.addEventListener('scroll', handleScroll);
    }

    // Create auto-load toggle button
    function createAutoLoadToggle() {
        const viewToggleButtons = document.querySelector('.view-toggle-buttons');
        if (!viewToggleButtons) return;

        const autoLoadButton = document.createElement('button');
        autoLoadButton.type = 'button';
        autoLoadButton.id = 'auto-load-toggle';
        autoLoadButton.className = 'btn btn-sm btn-outline-secondary';
        autoLoadButton.innerHTML = '<i class="fas fa-sync"></i>';
        autoLoadButton.title = '自動載入';
        // Always show the button, no need to hide initially

        autoLoadButton.addEventListener('click', toggleAutoLoad);

        viewToggleButtons.appendChild(autoLoadButton);
    }

    // Check view mode and show/hide auto-load button
    function checkViewMode() {
        const articleRow = document.getElementById('article-list-row');
        const autoLoadButton = document.getElementById('auto-load-toggle');

        if (!articleRow || !autoLoadButton) return;

        // Always display the auto-load button
        autoLoadButton.style.display = ''; 

        // Restore auto-load state from localStorage
        const savedState = localStorage.getItem('autoLoadEnabled') === 'true';
        if (savedState) {
            autoLoadEnabled = true;
            autoLoadButton.classList.add('active');
            if (autoLoadEnabled) { // Ensure spinning icon if active
                 autoLoadButton.innerHTML = '<i class="fas fa-sync fa-spin"></i>';
            }
        } else {
            autoLoadEnabled = false;
            autoLoadButton.classList.remove('active');
            autoLoadButton.innerHTML = '<i class="fas fa-sync"></i>';
        }
    }

    // Toggle auto-load
    function toggleAutoLoad() {
        const autoLoadButton = document.getElementById('auto-load-toggle');
        autoLoadEnabled = !autoLoadEnabled;

        if (autoLoadEnabled) {
            autoLoadButton.classList.add('active');
            autoLoadButton.innerHTML = '<i class="fas fa-sync fa-spin"></i>';
            showToast('自動載入已啟用', 'success');
        } else {
            autoLoadButton.classList.remove('active');
            autoLoadButton.innerHTML = '<i class="fas fa-sync"></i>';
            showToast('自動載入已停用', 'info');
        }

        localStorage.setItem('autoLoadEnabled', autoLoadEnabled.toString());
    }

    // Handle scroll event
    function handleScroll() {
        if (!autoLoadEnabled || isLoading || currentPage >= totalPages) return;

        const scrollPosition = window.innerHeight + window.scrollY;
        const documentHeight = document.documentElement.offsetHeight;

        // Load more when user is 200px from bottom
        if (scrollPosition >= documentHeight - 200) {
            loadNextPage();
        }
    }

    // Load next page
    async function loadNextPage() {
        isLoading = true;
        currentPage++;

        // Show loading indicator
        showLoadingIndicator();

        try {
            // Construct URL
            let url = window.location.pathname + '?page=' + currentPage;

            // Add user ID for user detail page
            if (window.location.pathname.includes('/user/')) {
                const userId = window.location.pathname.split('/').pop();
                url = `/user/${userId}?page=${currentPage}`;
            }

            const response = await fetch(url, {
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });

            if (!response.ok) throw new Error('Failed to load page');

            const html = await response.text();
            const parser = new DOMParser();
            const doc = parser.parseFromString(html, 'text/html');

            // Extract articles
            const newArticles = doc.querySelectorAll('.article-item');
            const articleRow = document.getElementById('article-list-row');

            if (newArticles.length > 0) {
                newArticles.forEach((article, index) => {
                    // Add animation delay
                    article.style.animationDelay = `${index * 0.1}s`;
                    articleRow.appendChild(article);
                });

                // Update pagination display
                updatePaginationDisplay();
            }

            hideLoadingIndicator();
        } catch (error) {
            console.error('Error loading next page:', error);
            showToast('載入失敗，請重試', 'error');
            hideLoadingIndicator();
            currentPage--; // Revert page number
        }

        isLoading = false;
    }

    // Show loading indicator
    function showLoadingIndicator() {
        const articleRow = document.getElementById('article-list-row');
        const loader = document.createElement('div');
        loader.className = 'col-12 text-center py-4';
        loader.id = 'auto-load-indicator';
        loader.innerHTML = `
            <div class="spinner-border text-primary" role="status">
                <span class="sr-only">載入中...</span>
            </div>
            <p class="mt-2 text-muted">正在載入更多文章...</p>
        `;
        articleRow.parentNode.insertBefore(loader, articleRow.nextSibling);
    }

    // Hide loading indicator
    function hideLoadingIndicator() {
        const loader = document.getElementById('auto-load-indicator');
        if (loader) loader.remove();
    }

    // Update pagination display
    function updatePaginationDisplay() {
        const paginationContainer = document.querySelector('.pagination-container');
        if (!paginationContainer) return;

        // Add indicator showing current loaded pages
        let indicator = document.getElementById('page-indicator');
        if (!indicator) {
            indicator = document.createElement('div');
            indicator.id = 'page-indicator';
            indicator.className = 'text-center mt-3 mb-3';
            paginationContainer.parentNode.insertBefore(indicator, paginationContainer);
        }

        indicator.innerHTML = `<span class="badge badge-info">已載入 ${currentPage} / ${totalPages} 頁</span>`;

        // Hide pagination if all pages are loaded
        if (currentPage >= totalPages) {
            paginationContainer.style.display = 'none';
            showToast('已載入所有文章', 'info');
        }
    }

    // Show toast notification
    function showToast(message, type = 'info') {
        const toastContainer = document.querySelector('.my-toast-container') || createToastContainer();

        const toast = document.createElement('div');

        // 根據類型設置正確的 Bootstrap 類
        const alertClass = type === 'error' ? 'danger' : type;
        toast.className = `alert alert-${alertClass} alert-dismissible fade show`;
        toast.innerHTML = `
            ${message}
            <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                <span aria-hidden="true">&times;</span>
            </button>
        `;

        toastContainer.appendChild(toast);

        // 移除舊的樣式設置，讓CSS來處理
        const closeBtn = toast.querySelector('.close');
        if (closeBtn) {
            closeBtn.style.opacity = '1';
            closeBtn.style.textShadow = 'none';
            closeBtn.style.color = 'inherit';
        }

        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 150);
        }, 3000);
    }

    // Create toast container
    function createToastContainer() {
        const container = document.createElement('div');
        container.className = 'my-toast-container';
        document.body.appendChild(container);
        return container;
    }

    // Listen for view mode changes
    document.addEventListener('DOMContentLoaded', function() {
        initAutoLoad();

        // Monitor view mode changes
        const observer = new MutationObserver(function(mutations) {
            mutations.forEach(function(mutation) {
                if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
                    checkViewMode();
                }
            });
        });

        const articleRow = document.getElementById('article-list-row');
        if (articleRow) {
            observer.observe(articleRow, { attributes: true });
        }
    });

})();
