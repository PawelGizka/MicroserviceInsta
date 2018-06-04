from datetime import datetime

from django.db import models
from django.contrib.auth.models import User

class UserProfile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE, blank=False)

class Connection(models.Model):
    follower = models.ForeignKey(User, on_delete=models.CASCADE, related_name='first')
    following = models.ForeignKey(User, on_delete=models.CASCADE, related_name='second')

    class Meta:
        unique_together = ('follower', 'following',)

class Token(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    text = models.TextField()
    expire = models.DateTimeField()

    def isExpired(self):
        return datetime.now() > self.expire
