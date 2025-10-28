// Admin Sidebar Component
function renderAdminSidebar(activePage) {
  return `
    <!-- Sidebar -->
    <aside class="sidebar" id="sidebar">
      <div class="sidebar-header">
        <a href="dashboard.html" class="sidebar-brand">
          <div class="sidebar-logo">O</div>
          <span>管理后台</span>
        </a>
      </div>

      <nav class="sidebar-menu">
        <div class="menu-section">
          <div class="menu-section-title">概览</div>
          <a href="dashboard.html" class="menu-item ${activePage === 'dashboard' ? 'active' : ''}">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="7" height="7"></rect>
              <rect x="14" y="3" width="7" height="7"></rect>
              <rect x="14" y="14" width="7" height="7"></rect>
              <rect x="3" y="14" width="7" height="7"></rect>
            </svg>
            <span>仪表盘</span>
          </a>
        </div>

        <div class="menu-section">
          <div class="menu-section-title">内容管理</div>
          <a href="sections.html" class="menu-item ${activePage === 'sections' ? 'active' : ''}">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
              <polyline points="9 22 9 12 15 12 15 22"></polyline>
            </svg>
            <span>板块管理</span>
          </a>
          <a href="posts.html" class="menu-item ${activePage === 'posts' ? 'active' : ''}">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
              <polyline points="14 2 14 8 20 8"></polyline>
            </svg>
            <span>帖子管理</span>
          </a>
          <a href="comments.html" class="menu-item ${activePage === 'comments' ? 'active' : ''}">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
            </svg>
            <span>评论管理</span>
          </a>
        </div>

        <div class="menu-section">
          <div class="menu-section-title">用户管理</div>
          <a href="users.html" class="menu-item ${activePage === 'users' ? 'active' : ''}">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
              <circle cx="9" cy="7" r="4"></circle>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
            </svg>
            <span>用户列表</span>
          </a>
          <a href="roles.html" class="menu-item ${activePage === 'roles' ? 'active' : ''}">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
            </svg>
            <span>角色权限</span>
          </a>
        </div>
      </nav>

      <div class="sidebar-footer">
        <a href="../pages/home.html" class="btn btn-text btn-block">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width: 18px; height: 18px; margin-right: 8px;">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
            <polyline points="9 22 9 12 15 12 15 22"></polyline>
          </svg>
          返回前台
        </a>
      </div>
    </aside>
  `;
}

// Sidebar styles
function getAdminSidebarStyles() {
  return `
    .sidebar {
      width: 260px;
      background: var(--background);
      border-right: 1px solid var(--divider);
      display: flex;
      flex-direction: column;
      position: fixed;
      height: 100vh;
      overflow-y: auto;
    }

    .sidebar-header {
      padding: var(--space-lg);
      border-bottom: 1px solid var(--divider);
    }

    .sidebar-brand {
      display: flex;
      align-items: center;
      gap: var(--space-md);
      color: var(--text-primary);
      text-decoration: none;
      font-size: 18px;
      font-weight: 500;
    }

    .sidebar-logo {
      width: 36px;
      height: 36px;
      background: var(--primary);
      border-radius: var(--radius-sm);
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-weight: 700;
    }

    .sidebar-menu {
      flex: 1;
      padding: var(--space-lg);
    }

    .menu-section {
      margin-bottom: var(--space-xl);
    }

    .menu-section-title {
      font-size: 12px;
      font-weight: 600;
      color: var(--text-secondary);
      text-transform: uppercase;
      letter-spacing: 0.5px;
      margin-bottom: var(--space-sm);
    }

    .menu-item {
      display: flex;
      align-items: center;
      gap: var(--space-md);
      padding: var(--space-sm) var(--space-base);
      color: var(--text-secondary);
      text-decoration: none;
      border-radius: var(--radius-sm);
      transition: all var(--transition-fast);
      margin-bottom: var(--space-xs);
    }

    .menu-item:hover {
      background: var(--background-secondary);
      color: var(--text-primary);
    }

    .menu-item.active {
      background: var(--primary);
      color: white;
    }

    .menu-item svg {
      width: 20px;
      height: 20px;
    }

    .sidebar-footer {
      padding: var(--space-lg);
      border-top: 1px solid var(--divider);
    }

    @media (max-width: 959px) {
      .sidebar {
        transform: translateX(-100%);
        transition: transform 0.3s;
        z-index: 1000;
      }

      .sidebar.active {
        transform: translateX(0);
      }
    }
  `;
}

// Initialize sidebar
function initAdminSidebar(activePage) {
  // Insert sidebar HTML
  const sidebarContainer = document.getElementById('admin-sidebar-container');
  if (sidebarContainer) {
    sidebarContainer.innerHTML = renderAdminSidebar(activePage);
  }
  
  // Insert sidebar styles if not already present
  if (!document.getElementById('admin-sidebar-styles')) {
    const styleElement = document.createElement('style');
    styleElement.id = 'admin-sidebar-styles';
    styleElement.textContent = getAdminSidebarStyles();
    document.head.appendChild(styleElement);
  }
}
