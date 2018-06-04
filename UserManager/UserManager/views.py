from django.contrib.auth.decorators import login_required
from django.shortcuts import render, get_object_or_404, redirect
from django.http import HttpResponse, JsonResponse, HttpResponseForbidden, HttpResponseRedirect, HttpResponseBadRequest
from .models import Connection
from django.views.decorators.csrf import csrf_exempt
from django.views.decorators.http import require_POST
from django.contrib.auth import login, authenticate, logout
from django.contrib.auth.models import User
from django.core.exceptions import PermissionDenied
from django.urls import reverse

# import the logging library
import logging

# Get an instance of a logger
logger = logging.getLogger(__name__)

def index(request):
    return render(request, 'index.html')

def login_form(request):
    return render(request, 'login.html', {'incorrect_login': False})

def register_form(request):
    return render(request, 'register.html', {'incorrect_login': False})

def do_logout(request):
    logout(request)
    return HttpResponseRedirect(reverse('index'))

def incorrect_login_form(request):
    return render(request, 'login.html', {'incorrect_login': True})

def incorrect_register_form(request):
    return render(request, 'register.html', {'incorrect_login': True})

def do_login(request):
    if 'username' in request.POST and 'password' in request.POST:
        username = request.POST['username']
        password = request.POST['password']
        user = authenticate(request, username=username, password=password)
        if user is not None:
            login(request, user)
            return HttpResponseRedirect(reverse('main_friends'))
        else:
            return HttpResponseRedirect(reverse('incorrect_login_form'))
    else:
        return HttpResponseBadRequest("request must contain username and password")

def do_register(request):
    if 'username' in request.POST and 'password' in request.POST and 'email' in request.POST:
        username = request.POST['username']
        password = request.POST['password']
        email = request.POST['email']
        user = User.objects.create_user(username, email, password)
        logger.info("created user")
        logger.info(user)
        user = authenticate(request, username=username, password=password)
        if user is not None:
            login(request, user)
            return HttpResponseRedirect(reverse('main_friends'))
        else:
            return HttpResponseRedirect(reverse('incorrect_register_form'))
    else:
        return HttpResponseBadRequest("request must contain username and password")

@login_required
def main_friends(request):
    return render(request, 'main_friends.html')

@login_required
def get_friends(request):
    users = User.objects.all()
    usersParsed = []
    for user in users:
        connection = Connection.objects.filter(follower=request.user).filter(following=user.id)
        if connection:
            usersParsed.append({'username': user.username, 'id': user.id, 'follows': True})
        else:
            usersParsed.append({'username': user.username, 'id': user.id, 'follows': False})
    return JsonResponse({'users': list(usersParsed)})


@login_required
@csrf_exempt
def follow(request):
    if 'id' not in request.POST:
        raise PermissionDenied
    following = User.objects.get(id=request.POST['id'])

    existingConnection = Connection.objects.filter(follower=request.user).filter(following=following)

    if existingConnection.count() == 0:
        Connection.objects.create(follower=request.user, following=following)
    else:
        existingConnection.delete()

    return HttpResponse()

@csrf_exempt
def get_followings_request(request):
    if 'HTTP_USERNAME' not in request.META or 'HTTP_PASSWORD' not in request.META:
        raise PermissionDenied
    if authenticate(username=request.META['HTTP_USERNAME'], password=request.META['HTTP_PASSWORD']) is None:
        raise PermissionDenied
    user = User.objects.get(username=request.META['HTTP_USERNAME'])
    followings = Connection.objects.filter(follower=user).values('following')
    return JsonResponse({ 'followings': list(followings) })