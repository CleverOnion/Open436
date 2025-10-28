// Common JavaScript Functions

// Modal Management
class Modal {
  constructor(modalId) {
    this.modal = document.getElementById(modalId);
    this.overlay = this.modal?.closest('.modal-overlay');
    this.init();
  }

  init() {
    if (!this.overlay) return;
    
    // Close on overlay click
    this.overlay.addEventListener('click', (e) => {
      if (e.target === this.overlay) {
        this.close();
      }
    });

    // Close on ESC key
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && this.overlay.classList.contains('active')) {
        this.close();
      }
    });
  }

  open() {
    this.overlay?.classList.add('active');
    document.body.style.overflow = 'hidden';
  }

  close() {
    this.overlay?.classList.remove('active');
    document.body.style.overflow = '';
  }
}

// Dropdown Management
function initDropdowns() {
  document.querySelectorAll('[data-dropdown]').forEach(dropdown => {
    const trigger = dropdown.querySelector('[data-dropdown-trigger]');
    
    trigger?.addEventListener('click', (e) => {
      e.stopPropagation();
      
      // Close other dropdowns
      document.querySelectorAll('[data-dropdown].active').forEach(other => {
        if (other !== dropdown) {
          other.classList.remove('active');
        }
      });
      
      dropdown.classList.toggle('active');
    });
  });

  // Close dropdowns when clicking outside
  document.addEventListener('click', () => {
    document.querySelectorAll('[data-dropdown].active').forEach(dropdown => {
      dropdown.classList.remove('active');
    });
  });
}

// Toast Notification
function showToast(message, type = 'info') {
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.textContent = message;
  
  const container = document.getElementById('toast-container') || createToastContainer();
  container.appendChild(toast);
  
  setTimeout(() => toast.classList.add('show'), 10);
  
  setTimeout(() => {
    toast.classList.remove('show');
    setTimeout(() => toast.remove(), 300);
  }, 3000);
}

function createToastContainer() {
  const container = document.createElement('div');
  container.id = 'toast-container';
  container.style.cssText = `
    position: fixed;
    top: 80px;
    right: 24px;
    z-index: 2000;
    display: flex;
    flex-direction: column;
    gap: 8px;
  `;
  document.body.appendChild(container);
  return container;
}

// Add toast styles
const toastStyles = `
  .toast {
    padding: 12px 16px;
    border-radius: 4px;
    color: white;
    font-size: 14px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    transform: translateX(400px);
    transition: transform 0.3s cubic-bezier(0.4, 0.0, 0.2, 1);
    min-width: 250px;
  }
  .toast.show {
    transform: translateX(0);
  }
  .toast-info { background: #2196F3; }
  .toast-success { background: #4CAF50; }
  .toast-warning { background: #FF9800; }
  .toast-error { background: #F44336; }
`;

const styleSheet = document.createElement('style');
styleSheet.textContent = toastStyles;
document.head.appendChild(styleSheet);

// Confirm Dialog
function showConfirm(message, onConfirm) {
  const overlay = document.createElement('div');
  overlay.className = 'modal-overlay active';
  overlay.innerHTML = `
    <div class="modal">
      <div class="modal-header">
        <h3 class="modal-title">确认操作</h3>
      </div>
      <div class="modal-body">
        <p>${message}</p>
      </div>
      <div class="modal-footer">
        <button class="btn btn-text" data-action="cancel">取消</button>
        <button class="btn btn-primary" data-action="confirm">确认</button>
      </div>
    </div>
  `;
  
  document.body.appendChild(overlay);
  document.body.style.overflow = 'hidden';
  
  overlay.querySelector('[data-action="cancel"]').addEventListener('click', () => {
    overlay.remove();
    document.body.style.overflow = '';
  });
  
  overlay.querySelector('[data-action="confirm"]').addEventListener('click', () => {
    onConfirm();
    overlay.remove();
    document.body.style.overflow = '';
  });
  
  overlay.addEventListener('click', (e) => {
    if (e.target === overlay) {
      overlay.remove();
      document.body.style.overflow = '';
    }
  });
}

// Format Date
function formatDate(dateString) {
  const date = new Date(dateString);
  const now = new Date();
  const diff = now - date;
  
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);
  
  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 7) return `${days}天前`;
  
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  });
}

// Format Number
function formatNumber(num) {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w';
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k';
  }
  return num.toString();
}

// Copy to Clipboard
function copyToClipboard(text) {
  if (navigator.clipboard) {
    navigator.clipboard.writeText(text).then(() => {
      showToast('已复制到剪贴板', 'success');
    });
  } else {
    const textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.style.position = 'fixed';
    textarea.style.opacity = '0';
    document.body.appendChild(textarea);
    textarea.select();
    document.execCommand('copy');
    document.body.removeChild(textarea);
    showToast('已复制到剪贴板', 'success');
  }
}

// Image Preview
function initImagePreview() {
  document.querySelectorAll('.post-content img, .reply-content img').forEach(img => {
    img.style.cursor = 'pointer';
    img.addEventListener('click', () => {
      const overlay = document.createElement('div');
      overlay.className = 'image-preview-overlay';
      overlay.innerHTML = `
        <div class="image-preview-container">
          <img src="${img.src}" alt="${img.alt || ''}">
          <button class="image-preview-close">&times;</button>
        </div>
      `;
      
      document.body.appendChild(overlay);
      document.body.style.overflow = 'hidden';
      
      setTimeout(() => overlay.classList.add('active'), 10);
      
      const close = () => {
        overlay.classList.remove('active');
        setTimeout(() => {
          overlay.remove();
          document.body.style.overflow = '';
        }, 300);
      };
      
      overlay.addEventListener('click', (e) => {
        if (e.target === overlay || e.target.classList.contains('image-preview-close')) {
          close();
        }
      });
    });
  });
}

// Add image preview styles
const imagePreviewStyles = `
  .image-preview-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.9);
    z-index: 3000;
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.3s;
  }
  .image-preview-overlay.active {
    opacity: 1;
  }
  .image-preview-container {
    position: relative;
    max-width: 90vw;
    max-height: 90vh;
  }
  .image-preview-container img {
    max-width: 100%;
    max-height: 90vh;
    object-fit: contain;
  }
  .image-preview-close {
    position: absolute;
    top: -40px;
    right: 0;
    background: transparent;
    border: none;
    color: white;
    font-size: 36px;
    cursor: pointer;
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
`;

const imageStyleSheet = document.createElement('style');
imageStyleSheet.textContent = imagePreviewStyles;
document.head.appendChild(imageStyleSheet);

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
  initDropdowns();
  initImagePreview();
});

// Export functions for use in other scripts
window.Modal = Modal;
window.showToast = showToast;
window.showConfirm = showConfirm;
window.formatDate = formatDate;
window.formatNumber = formatNumber;
window.copyToClipboard = copyToClipboard;
