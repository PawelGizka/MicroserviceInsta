"""UserManager URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/2.0/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
from django.urls import path
from . import views

urlpatterns = [
    path('admin/', admin.site.urls),
    path('', views.index, name='index'),
    path('main_friends', views.main_friends, name='main_friends'),
    path('login_form', views.login_form, name='login_form'),
    path('register_form', views.register_form, name='register_form'),
    path('incorrect_login_form', views.incorrect_login_form, name='incorrect_login_form'),
    path('incorrect_register_form', views.incorrect_register_form, name='incorrect_register_form'),
    path('login', views.do_login, name='login'),
    path('register', views.do_register, name='register'),
    path('logout', views.do_logout, name='logout'),
    path('ajax/get_friends/', views.get_friends, name='get_friends'),
    path('ajax/follow/', views.follow, name='follow'),
    path('main_upload', views.main_upload, name='main_upload'),
    path('get_user_info/', views.get_followings_request, name='get_followings_request'),
    path('main_photos', views.main_photos, name='main_photos'),
]
