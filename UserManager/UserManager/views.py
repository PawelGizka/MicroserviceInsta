import random
import string

from django.contrib.auth.decorators import login_required
from django.shortcuts import render, get_object_or_404, redirect
from django.http import HttpResponse, JsonResponse, HttpResponseForbidden, HttpResponseRedirect, HttpResponseBadRequest
from .models import Connection, Token
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
            createToken(user)
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
            createToken(user)
            return HttpResponseRedirect(reverse('main_friends'))
        else:
            return HttpResponseRedirect(reverse('incorrect_register_form'))
    else:
        return HttpResponseBadRequest("request must contain username and password")

@login_required
def main_friends(request):
    return render(request, 'main_friends.html')

@login_required
def main_upload(request):
    return render(request, 'main_upload.html', {'token': fetchToken(request.user)})

@login_required
def main_photos(request):
    return render(request, 'main_photos.html', {'token': fetchToken(request.user)})

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
    logger.info("before")

    if 'HTTP_AUTHENTICATION' not in request.META:
        raise PermissionDenied

    logger.info("after")

    user = fetchUser(request.META['HTTP_AUTHENTICATION'])
    connections = Connection.objects.filter(follower=user)
    followingsParsed = []
    for connection in connections:
        followingsParsed.append({'id': connection.following.id, 'username': connection.following.username})

    return JsonResponse({ 'userId': user.id, 'friends': list(followingsParsed) })

def createToken(user):
    alreadyExistingToken = Token.objects.filter(user=user)
    alreadyExistingToken.delete()
    tokenString = str(user.id) + '-' + ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(50))
    Token.objects.create(user=user, token=tokenString)

def fetchToken(user):
    return Token.objects.filter(user=user).first().token

def fetchUser(token):
    return Token.objects.filter(token__exact=token).first().user
