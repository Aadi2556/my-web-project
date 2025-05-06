document.addEventListener('DOMContentLoaded', async () => {
    const params = new URLSearchParams(window.location.search);
    const courseId = params.get('courseId');
    const token = localStorage.getItem('instructorJwtToken');
    
    if (!courseId || !token) window.location.href = 'instructorlogin.html';

    try {
        // Load course data
        const response = await fetch(`http://localhost:8080/Web/secure/courseGet?courseId=${courseId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const course = await response.json();
        
        // Populate form
        document.getElementById('courseId').value = course.courseId;
        document.getElementById('title').value = course.title;
        document.getElementById('description').value = course.description;
        
        // Display existing files
        const fileList = document.getElementById('fileList');
        course.files.forEach(file => {
            const div = document.createElement('div');
            div.className = 'file-item';
            div.innerHTML = `
                ${file} 
                <span class="file-delete" onclick="deleteFile('${file}')">✖</span>
            `;
            fileList.appendChild(div);
        });
    } catch (error) {
        alert('Error loading course');
    }

    // Form submission
    document.getElementById('editForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const formData = new FormData();
        formData.append('courseId', courseId);
        formData.append('title', document.getElementById('title').value);
        formData.append('description', document.getElementById('description').value);
        
        // Add new files
        const files = document.getElementById('newFiles').files;
        for (let i = 0; i < files.length; i++) {
            formData.append('files', files[i]);
        }
        
        try {
            const response = await fetch('http://localhost:8080/Web/secure/courseUpdate', {
                method: 'PUT',
                headers: { 'Authorization': `Bearer ${token}` },
                body: formData
            });
            
            const result = await response.json();
            if (result.status === 'success') {
                window.location.href = 'instructordashboard.html';
            }
        } catch (error) {
            document.getElementById('message').textContent = 'Update failed';
        }
    });
});

async function deleteFile(filename) {
    if (!confirm('Delete this file?')) return;
    
    try {
        const encodedFilename = encodeURIComponent(filename);
        const response = await fetch(
            `http://localhost:8080/Web/secure/fileDelete?filename=${encodedFilename}`, 
            {
                method: 'DELETE',
                headers: { 
                    'Authorization': `Bearer ${localStorage.getItem('instructorJwtToken')}`
                }
            }
        );

        const result = await response.json();
        if (result.status === 'success') {
            // Find and remove the file element safely
            document.querySelectorAll('.file-item').forEach(element => {
                if (element.textContent.includes(filename)) {
                    element.remove();
                }
            });
        } else {
            alert('Delete failed: ' + (result.message || 'Unknown error'));
        }
    } catch (error) {
        alert('File deletion failed: ' + error.message);
    }
}